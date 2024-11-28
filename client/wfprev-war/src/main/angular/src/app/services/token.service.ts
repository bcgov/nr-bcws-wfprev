import { Injectable, Injector } from "@angular/core";
import { HttpClient, HttpHandler, HttpHeaders, HttpResponse } from "@angular/common/http";
import { OAuthService } from "angular-oauth2-oidc";
import momentInstance from "moment";
import { AsyncSubject, Observable, catchError, firstValueFrom, map, of } from "rxjs";
import { AppConfigService } from "./app-config.service";

const moment = momentInstance;
const OAUTH_LOCAL_STORAGE_KEY = 'oauth';

interface ValidationResponse {
  status: string
}

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  private LOCAL_STORAGE_KEY = OAUTH_LOCAL_STORAGE_KEY;
  private useLocalStore = false;
  private oauth: any;
  private tokenDetails: any;

  private credentials = new AsyncSubject<any>();
  private authToken = new AsyncSubject<string>();
  public credentialsEmitter: Observable<any> = this.credentials.asObservable();
  public authTokenEmitter: Observable<string> = this.authToken.asObservable();

  constructor(private injector: Injector, protected appConfigService: AppConfigService) {
    const config = this.appConfigService.getConfig().application;

    const lazyAuthenticate: boolean = config.lazyAuthenticate ?? false;
    const enableLocalStorageToken: boolean = config.enableLocalStorageToken ?? false;
    const localStorageTokenKey: string = config.localStorageTokenKey ?? OAUTH_LOCAL_STORAGE_KEY;
    const allowLocalExpiredToken: boolean = config.allowLocalExpiredToken ?? false;

    if (localStorageTokenKey) {
      this.LOCAL_STORAGE_KEY = localStorageTokenKey;
    }

    if (enableLocalStorageToken) {
      this.useLocalStore = true;
    }

    this.checkForToken(undefined, lazyAuthenticate, allowLocalExpiredToken);
  }

  public checkForToken(redirectUri?: string, lazyAuth = false, allowLocalExpiredToken = false): void {
    const hash = window.location.hash;

    if (hash && hash.includes('access_token')) {
      this.parseToken(hash);
    } else if (this.useLocalStore && !navigator.onLine) {
      let tokenStore = localStorage.getItem(this.LOCAL_STORAGE_KEY);

      if (tokenStore) {
        try {
          tokenStore = JSON.parse(tokenStore);
          this.initAuthFromSession();
        } catch (err) {
          console.log('Failed to read session token - reinitializing');
          this.tokenDetails = undefined;
          localStorage.removeItem(this.LOCAL_STORAGE_KEY);
          this.initImplicitFlow(redirectUri);
        }
      } else {
        this.initImplicitFlow(redirectUri);
      }

      if (!allowLocalExpiredToken && this.isTokenExpired(this.tokenDetails)) {
        localStorage.removeItem(this.LOCAL_STORAGE_KEY);
        this.initImplicitFlow(redirectUri);
      }
    } else if (hash && hash.includes('error')) {
      alert('Error occurred during authentication.');
      return;
    } else if (!lazyAuth) {
      this.initImplicitFlow(redirectUri);
    }
  }

  public isTokenExpired(token: any): boolean {
    if (token?.exp) {
      const expiryDate = moment.unix(token.exp);
      return !moment().isBefore(expiryDate);
    }
    return true;
  }

  private parseToken(hash: string): void {
    hash = hash.startsWith('#') ? hash.substr(1) : hash;
    const params = new URLSearchParams(hash);
    const paramMap: { [key: string]: string } = {};

    params.forEach((value, key) => {
      paramMap[key] = value;
    });

    if (paramMap['access_token']) {
      location.hash = '';
      this.initAuth(paramMap);
    }
  }

  private initImplicitFlow(redirectUri?: string): void {
    const configuration = this.appConfigService.getConfig();
    const authConfig = {
      oidc: false,
      issuer: configuration.application.baseUrl,
      loginUrl: configuration.webade.oauth2Url,
      redirectUri: redirectUri ?? window.location.href,
      clientId: configuration.webade.clientId,
      scope: configuration.webade.authScopes
    };

    const oauthService = this.injector.get(OAuthService);
    oauthService.configure(authConfig);
    oauthService.initImplicitFlow();
  }

  public initRefreshTokenImplicitFlow(authorizeURL: string, storageKey: string, errorCallback: (message: string) => void): Observable<any> {
    const options = 'resizable=yes,scrollbars=yes,statusbar=yes,status=yes';
    let refreshWindow = window.open(authorizeURL, "", options);
    const refreshAsync = new AsyncSubject<any>();

    const refreshInterval = setInterval(() => {
      if (!refreshWindow) {
        errorCallback('Session Expired. Unable to open refresh window. Please allow pop-ups.');
        refreshWindow = window.open(authorizeURL, "", options);
      }

      if (refreshWindow?.closed) {
        clearInterval(refreshInterval);
        let newToken = localStorage.getItem(`${storageKey}`);
        newToken = newToken ? JSON.parse(newToken) : null;
        localStorage.removeItem(`${storageKey}`);
        if (newToken) {
          this.updateToken(newToken);
          refreshAsync.next(newToken);
          refreshAsync.complete();
        }
      }
    }, 500);

    return refreshAsync.asObservable();
  }

  private async initAuthFromSession(): Promise<void> {
    try {
      const localOauth = localStorage.getItem(this.LOCAL_STORAGE_KEY);
      this.oauth = localOauth ? JSON.parse(localOauth) : null;
      await this.initAndEmit();
    } catch (err) {
      localStorage.removeItem(this.LOCAL_STORAGE_KEY);
      console.log('Failed to handle token payload', this.oauth);
      this.handleError(err, 'Failed to handle token');
    }
  }

  public initAuth(response: any): void {
    if (response) {
      try {
        if (this.useLocalStore) {
          const tokenStore = {
            access_token: response.access_token,
            expires_in: response.expires_in
          };
          localStorage.setItem(this.LOCAL_STORAGE_KEY, JSON.stringify(tokenStore));
        }
        this.oauth = response;
        this.initAndEmit();
      } catch (err) {
        if (this.useLocalStore) {
          localStorage.removeItem(this.LOCAL_STORAGE_KEY);
        }
        console.log('Failed to handle token payload', this.oauth);
        this.handleError(err, 'Failed to handle token');
      }
    }
  }

  public validateToken(token: string): Observable<boolean> {
    const http = new HttpClient(this.injector.get(HttpHandler));
    const headers = new HttpHeaders({
      'Authorization': 'Bearer ' + `${token}`,
      'Access-Control-Allow-Origin': '*'
    });
    return http.get(
      "http://localhost:8080/pub/wfprev-api/check/checkToken",
      {
        headers,
        observe: 'response'
      }
    ).pipe(
      map(response => response.status === 200),
      catchError(() => of(false))
    );

  }


  private async initAndEmit(): Promise<void> {
    const config = this.appConfigService.getConfig();

    if (config.webade.enableCheckToken) {
      let baseUrl = config.application.baseUrl;
      baseUrl = baseUrl.endsWith('/') ? baseUrl : baseUrl.concat('/');
      const checkTokenUrl = `${baseUrl}${config.webade.checkTokenUrl}`;

      const headers = new HttpHeaders({
        'Authorization': `Bearer ${this.oauth.access_token}`,
      });

      try {
        const http = new HttpClient(this.injector.get(HttpHandler));
        this.tokenDetails = await firstValueFrom(http.get(checkTokenUrl, { headers }));
        this.emitTokens();
      } catch (error: any) {
        console.log(error);
        alert(`App initialization Failed ${error.status}. Status(Check token failed)`);
        throw error;
      }
    } else {
      const oauthInfo = this.oauth.access_token.split('.');

      if (oauthInfo.length > 1) {
        this.tokenDetails = JSON.parse(atob(oauthInfo[1]));
      }

      this.emitTokens();
    }
  }

  private emitTokens(): void {
    this.authToken.next(this.oauth.access_token);
    this.authToken.complete();
    this.credentials.next(this.tokenDetails);
    this.credentials.complete();
  }

  public updateToken(oauthToken: any): void {
    this.oauth = oauthToken;
    this.initAndEmit();
  }

  public getOauthToken(): string | null {
    return this.oauth?.access_token ?? null;
  }

  public getTokenDetails(): any | null {
    return this.tokenDetails ?? null;
  }

  public doesUserHaveApplicationPermissions(scopes?: string[]): boolean {
    if (this.tokenDetails?.scope?.length > 0 && scopes?.length) {
      return scopes.every(scope => this.tokenDetails.scope.includes(scope));
    }
    return false;
  }

  public clearLocalStorageToken(): void {
    localStorage.removeItem(this.LOCAL_STORAGE_KEY);
  }

  private handleError(err: any, message?: string): never {
    console.error('Unexpected error', err);
    alert(message ? `${message} ${err}` : `${err}`);
    throw err;
  }
}


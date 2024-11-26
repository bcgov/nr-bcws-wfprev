import { AuthGuard } from './auth-guard';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import {
    ActivatedRouteSnapshot,
    Router,
    RouterStateSnapshot,
} from '@angular/router';
import { Injectable } from '@angular/core';
import { AsyncSubject, Observable, of, firstValueFrom, from } from 'rxjs';
import { catchError, map, mergeMap } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ResourcesRoutes } from '../../utils';

@Injectable({
    providedIn: 'root',
})
export class PrevAuthGuard extends AuthGuard {
    private asyncCheckingToken: any;

    constructor(
        tokenService: TokenService,
        router: Router,
        private appConfigService: AppConfigService,
        protected snackbarService: MatSnackBar,
    ) {
        super(tokenService, router);
        this.baseScopes = [];
    }

    override canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot,
    ): Observable<boolean> {

        if (!window.navigator.onLine) {
            return of(false);
        }

        if (route?.data?.['scopes']?.length > 0) {
            // Wrap the Promise returned by getTokenInfo with from()
            return from(this.getTokenInfo(route)).pipe(
                map(result => result),
                catchError(() => of(false))
            );
        } else {
            console.log('returning true');
            return of(true);
        }
    }
    async getTokenInfo(route: any) {
        if (!this.tokenService.getOauthToken()) {
            if (this.asyncCheckingToken) {
                return this.asyncCheckingToken;
            }
            let redirectUri = this.appConfigService.getConfig().application.baseUrl;
            const path = route.routeConfig.path;
            let pathWithParamSubs = path;
            let queryParamStr = '?';
            if (route.params) {
                Object.keys(route.params).forEach((paramKey) => {
                    pathWithParamSubs = pathWithParamSubs.replace(
                        ':' + paramKey,
                        route.params[paramKey],
                    );
                });
            }
            redirectUri = redirectUri.concat(pathWithParamSubs);
            if (route.queryParams) {
                Object.keys(route.queryParams).forEach((paramKey) => {
                    queryParamStr += paramKey + '=' + route.queryParams[paramKey] + '&';
                });
                queryParamStr = queryParamStr.substr(0, queryParamStr.length - 1);
                redirectUri = redirectUri.concat(queryParamStr);
            }
            return this.checkForToken(redirectUri, route).pipe(
                mergeMap((result) => {
                    this.asyncCheckingToken = undefined;
                    if (!result) {
                        this.redirectToErrorPage();
                        return of(result);
                    } else {
                        return of(result);
                    }
                }),
            );
            // call to spring boot backend to authenticate token/scopes
        } else if (await this.canAccessRoute(route.data.scopes, this.tokenService)) {
            return of(true);
        } else {
            this.redirectToErrorPage();
        }
    }

    async canAccessRoute(scopes: string[][], tokenService: TokenService) {
        const token = this.tokenService.getOauthToken();
        if (!token) {
            return false;
        }
        try {
            const response = await firstValueFrom(this.tokenService.validateToken(token));
            return response || false; // Ensure `response` is boolean
        } catch (error) {
            console.error("Error validating token:", error);
            return false; // Handle error gracefully
        }
    }

    override redirectToErrorPage() {
        this.router.navigate(['/' + ResourcesRoutes.ERROR_PAGE]);
    }

    reloadPage() {
        document.location.reload();
    }

    checkForToken(
        redirectUri: string,
        route: ActivatedRouteSnapshot,
    ): Observable<boolean> {
        if (this.asyncCheckingToken) {
            return this.asyncCheckingToken;
        }

        this.asyncCheckingToken = new AsyncSubject();
        this.tokenService.checkForToken(redirectUri);

        this.tokenService.authTokenEmitter.subscribe(() => {
            if (!this.canAccessRoute(route.data['scopes'], this.tokenService)) {
                this.asyncCheckingToken.next(false);
                this.asyncCheckingToken.complete();
            } else {
                this.asyncCheckingToken.next(true);
                this.asyncCheckingToken.complete();
            }
        });
        return this.asyncCheckingToken;
    }
}


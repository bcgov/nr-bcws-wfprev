import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router";
import {TokenService} from "../token.service";
import {Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  protected credentials: any;
  protected baseScopes = []; 
  constructor(protected tokenService: TokenService, protected router: Router) {
    this.tokenService.credentialsEmitter.subscribe(credentials => {
      this.credentials = credentials;
    });
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean> | boolean {
    let url: string = state.url;
    let result = this.checkLogin(url, this.baseScopes);
    return of(result);
  }

  checkLogin(url: string, scopes: string[]): boolean {
    console.log('Checking login with:');
    console.log('URL:', url);
    console.log('Scopes:', scopes);
    console.log('This credentials:', this.credentials);
    console.log('Credentials subject:', this.tokenService.credentialsEmitter);
  
    // Always call doesUserHaveApplicationPermissions, even when no credentials
    let isAuthorized = this.tokenService.doesUserHaveApplicationPermissions(scopes);
   
    // If credentials are present, && with the credentials check
    if (this.credentials) {
      isAuthorized = isAuthorized && !!this.credentials;
    } else {
      isAuthorized = false;
    }
  
    if (!isAuthorized) {
      this.redirectToErrorPage();
    }
    return isAuthorized;

  }

  redirectToErrorPage() {
    // Navigate to the unauthorized page
    this.router.navigate([{ outlets: { root: ['unauthorized'] } }]);
  }
}

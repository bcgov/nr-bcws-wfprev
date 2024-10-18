import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router";
import {TokenService} from "../token.service";
import {Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  protected credentials: any;
  protected baseScopes = []; //["WFRM.GET_CODE_TABLES","WFRM.GET_TOPLEVEL"];
  constructor(protected tokenService: TokenService, protected router: Router) {
   // console.log("using auth guard");
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
    let isAuthorized = (this.credentials) ? this.tokenService.doesUserHaveApplicationPermissions(scopes) : false;

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

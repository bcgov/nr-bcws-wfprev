import { AuthGuard } from './auth-guard';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import {
    ActivatedRouteSnapshot,
    Router,
    RouterStateSnapshot,
} from '@angular/router';
import { Injectable } from '@angular/core';
import { AsyncSubject, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
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
            return this.getTokenInfo(route);
        } else {
            return of(true);
        }
    }

    getTokenInfo(route: any) {
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
        } else if (this.canAccessRoute(route.data.scopes, this.tokenService)) {
            return of(true);
        } else {
            this.redirectToErrorPage();
        }
    }

    canAccessRoute(scopes: string[][], tokenService: TokenService): boolean {
        if (this.tokenService.getOauthToken()) {
            return true;
        }
        return false;
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

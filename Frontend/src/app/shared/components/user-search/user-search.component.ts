import {
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, Subscription, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap, tap } from 'rxjs/operators';

import { UserApiService } from '../../../core/services/user-api.service';
import { SidebarSuggestion } from '../../../core/models/user-sidebar.model';
import { UserAvatarComponent } from '../user-avatar/user-avatar.component';

@Component({
  selector: 'app-user-search',
  standalone: true,
  imports: [FormsModule, UserAvatarComponent],
  templateUrl: './user-search.component.html',
})
export class UserSearchComponent implements OnInit, OnDestroy {
  private readonly userApi = inject(UserApiService);
  private readonly router = inject(Router);
  private readonly host = inject(ElementRef<HTMLElement>);

  readonly query = signal('');
  readonly results = signal<SidebarSuggestion[]>([]);
  readonly loading = signal(false);
  readonly open = signal(false);

  private readonly query$ = new Subject<string>();
  private subscription?: Subscription;

  ngOnInit(): void {
    this.subscription = this.query$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap((q) => {
          if (q.trim().length === 0) {
            this.results.set([]);
            this.loading.set(false);
          } else {
            this.loading.set(true);
          }
        }),
        switchMap((q) => {
          const trimmed = q.trim();
          if (trimmed.length === 0) {
            return of<SidebarSuggestion[]>([]);
          }
          return this.userApi.searchUsers(trimmed, 10).pipe(
            catchError(() => of<SidebarSuggestion[]>([])),
          );
        }),
      )
      .subscribe((users) => {
        this.results.set(users);
        this.loading.set(false);
      });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.query$.complete();
  }

  onInput(value: string): void {
    this.query.set(value);
    this.open.set(true);
    this.query$.next(value);
  }

  onFocus(): void {
    if (this.query().trim().length > 0 || this.results().length > 0) {
      this.open.set(true);
    }
  }

  clear(): void {
    this.query.set('');
    this.results.set([]);
    this.open.set(false);
    this.query$.next('');
  }

  selectUser(user: SidebarSuggestion): void {
    this.open.set(false);
    this.query.set('');
    this.results.set([]);
    this.router.navigate(['/app/profile', user.username]);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as Node | null;
    if (!target || !this.host.nativeElement.contains(target)) {
      this.open.set(false);
    }
  }
}

import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { Subscription } from 'rxjs';
import { distinctUntilChanged, map, switchMap, tap, catchError } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { RoutineApiService } from '../../../../core/services/routine-api.service';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { Routine, routineEjerciciosToPostShape } from '../../../../core/models/routine.model';
import { WorkoutDetailExercises } from '../../../feed/components/workout-detail-exercises/workout-detail-exercises';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-routine-detail-page',
  standalone: true,
  imports: [DatePipe, RouterLink, WorkoutDetailExercises, UserAvatarComponent],
  templateUrl: './routine-detail-page.component.html',
})
export class RoutineDetailPageComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly routineApi = inject(RoutineApiService);
  private readonly currentUser = inject(CurrentUserService);

  readonly routine = signal<Routine | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly copySuccess = signal(false);
  readonly showDeleteModal = signal(false);
  readonly deleting = signal(false);

  readonly isOwner = computed(() => {
    const r = this.routine();
    const me = this.currentUser.profile()?.username;
    return !!r && !!me && r.usuario.username === me;
  });

  readonly ejerciciosForDisplay = computed(() => {
    const r = this.routine();
    return r ? routineEjerciciosToPostShape(r.ejercicios) : [];
  });

  private sub?: Subscription;

  ngOnInit(): void {
    this.sub = this.route.paramMap
      .pipe(
        map((pm) => pm.get('publicId')?.trim() ?? ''),
        distinctUntilChanged(),
        tap(() => {
          this.loading.set(true);
          this.errorMessage.set('');
        }),
        switchMap((publicId) => {
          if (!publicId) {
            this.errorMessage.set('Rutina no válida.');
            this.loading.set(false);
            return EMPTY;
          }
          return this.routineApi.getRutina(publicId).pipe(
            tap((r) => {
              this.routine.set(r);
              this.loading.set(false);
            }),
            catchError(() => {
              this.errorMessage.set('No se pudo cargar la rutina.');
              this.loading.set(false);
              return EMPTY;
            }),
          );
        }),
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  goBack(): void {
    this.router.navigate(['/app/routines']);
  }

  copyLink(): void {
    const r = this.routine();
    if (!r) return;
    const url = `${window.location.origin}/app/routines/${r.publicId}`;
    const markSuccess = () => {
      this.copySuccess.set(true);
      setTimeout(() => this.copySuccess.set(false), 2000);
    };

    if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(url).then(markSuccess).catch(() => this.copyFallback(url, markSuccess));
    } else {
      this.copyFallback(url, markSuccess);
    }
  }

  private copyFallback(text: string, onSuccess: () => void): void {
    const ta = document.createElement('textarea');
    ta.value = text;
    ta.style.cssText = 'position:fixed;top:0;left:0;opacity:0';
    document.body.appendChild(ta);
    ta.focus();
    ta.select();
    try {
      document.execCommand('copy');
      onSuccess();
    } catch {
      // silently ignore
    } finally {
      document.body.removeChild(ta);
    }
  }

  editRoutine(): void {
    const r = this.routine();
    if (r) this.router.navigate(['/app/routines', r.publicId, 'editar']);
  }

  openDeleteModal(): void {
    this.showDeleteModal.set(true);
  }

  cancelDelete(): void {
    this.showDeleteModal.set(false);
  }

  confirmDelete(): void {
    const r = this.routine();
    if (!r || this.deleting()) return;
    this.deleting.set(true);
    this.routineApi.deleteRutina(r.publicId).subscribe({
      next: () => {
        this.deleting.set(false);
        this.router.navigate(['/app/routines']);
      },
      error: () => {
        this.deleting.set(false);
        this.showDeleteModal.set(false);
        this.errorMessage.set('No se pudo eliminar la rutina.');
      },
    });
  }
}

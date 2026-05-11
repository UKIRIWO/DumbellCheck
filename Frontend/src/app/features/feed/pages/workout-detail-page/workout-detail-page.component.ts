import { Location } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, EMPTY } from 'rxjs';
import { catchError, distinctUntilChanged, map, switchMap, tap, finalize } from 'rxjs/operators';
import { PostApiService } from '../../../../core/services/post-api.service';
import { FeedSidebarContextService } from '../../../../core/services/feed-sidebar-context.service';
import { PostFeedItem } from '../../../../core/models/post.model';
import { WorkoutDetailHeader } from '../../components/workout-detail-header/workout-detail-header';
import { WorkoutDetailMedia } from '../../components/workout-detail-media/workout-detail-media';
import { WorkoutDetailExercises } from '../../components/workout-detail-exercises/workout-detail-exercises';
import { CommentsModalComponent } from '../../components/comments-modal/comments-modal.component';

@Component({
  selector: 'app-workout-detail-page',
  imports: [WorkoutDetailHeader, WorkoutDetailMedia, WorkoutDetailExercises, CommentsModalComponent],
  templateUrl: './workout-detail-page.component.html',
})
export class WorkoutDetailPageComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);
  private readonly postApi = inject(PostApiService);
  private readonly sidebarContext = inject(FeedSidebarContextService);

  private routeSub?: Subscription;

  post = signal<PostFeedItem | null>(null);
  loading = signal(true);
  errorMessage = signal('');
  commentsOpen = signal(false);
  liking = signal(false);

  ngOnInit(): void {
    this.routeSub = this.route.paramMap
      .pipe(
        map((pm) => pm.get('publicId')?.trim() ?? ''),
        distinctUntilChanged(),
        switchMap((publicId) => {
          this.sidebarContext.setHighlightedAuthor(null);
          if (!/^[A-Za-z0-9_-]{21}$/.test(publicId)) {
            this.post.set(null);
            this.loading.set(false);
            this.errorMessage.set('Publicación no válida.');
            return EMPTY;
          }
          this.loading.set(true);
          this.errorMessage.set('');
          return this.postApi.getPostByPublicId(publicId).pipe(
            tap({
              next: (response) => {
                this.post.set(response);
                this.sidebarContext.setHighlightedAuthor(response.usuario.username);
              },
            }),
            catchError(() => {
              this.post.set(null);
              this.errorMessage.set('No se pudo cargar la publicación.');
              return EMPTY;
            }),
            finalize(() => this.loading.set(false)),
          );
        }),
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.sidebarContext.setHighlightedAuthor(null);
  }

  goBack(): void {
    this.location.back();
  }

  get hasMedia(): boolean {
    return !!this.post()?.multimediaUrl;
  }

  openComments(): void {
    this.commentsOpen.set(true);
  }

  toggleLike(): void {
    const current = this.post();
    if (!current || this.liking()) return;

    this.liking.set(true);
    this.postApi.toggleLike(current.publicId).subscribe({
      next: (response) => {
        this.post.set({
          ...current,
          meGusta: response.meGusta,
          contadorLikes: response.contadorLikes,
        });
        this.liking.set(false);
      },
      error: () => this.liking.set(false),
    });
  }

  closeComments(): void {
    this.commentsOpen.set(false);
  }

  onCommentCreated(): void {
    const current = this.post();
    if (!current) return;
    this.post.set({
      ...current,
      contadorComentarios: (current.contadorComentarios ?? 0) + 1,
    });
  }

  onCommentDeleted(): void {
    const current = this.post();
    if (!current) return;
    this.post.set({
      ...current,
      contadorComentarios: Math.max(0, (current.contadorComentarios ?? 0) - 1),
    });
  }
}

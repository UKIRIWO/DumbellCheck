import { CommonModule, DatePipe } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CommentApiService } from '../../../../core/services/comment-api.service';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { Comment } from '../../../../core/models/comment.model';
import { PostFeedItem } from '../../../../core/models/post.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-comments-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePipe, UserAvatarComponent],
  templateUrl: './comments-modal.component.html',
})
export class CommentsModalComponent implements OnChanges {
  private readonly commentApi = inject(CommentApiService);
  private readonly currentUser = inject(CurrentUserService);

  @Input({ required: true }) post!: PostFeedItem;
  @Output() closed = new EventEmitter<void>();
  @Output() commentCreated = new EventEmitter<Comment>();

  readonly comments = signal<Comment[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly newComment = signal('');
  readonly submitting = signal(false);

  readonly currentUsername = computed(() => this.currentUser.profile()?.username ?? '');
  readonly currentFotoPerfilUrl = computed(
    () => this.currentUser.profile()?.fotoPerfilUrl ?? null,
  );

  readonly canSubmit = computed(
    () => !this.submitting() && this.newComment().trim().length > 0,
  );

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['post'] && this.post) {
      this.loadComments();
    }
  }

  private loadComments(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.commentApi.getComments(this.post.publicId).subscribe({
      next: (data) => {
        this.comments.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('No se pudieron cargar los comentarios.');
        this.loading.set(false);
      },
    });
  }

  submitComment(): void {
    const texto = this.newComment().trim();
    if (!texto || this.submitting()) return;

    this.submitting.set(true);
    this.commentApi
      .createComment(this.post.publicId, { texto })
      .subscribe({
        next: (created) => {
          this.comments.update((prev) => [...prev, created]);
          this.newComment.set('');
          this.submitting.set(false);
          this.commentCreated.emit(created);
        },
        error: () => {
          this.submitting.set(false);
          this.errorMessage.set('No se pudo publicar el comentario.');
        },
      });
  }

  onTextareaKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.submitComment();
    }
  }

  onBackdropClick(): void {
    this.closed.emit();
  }
}

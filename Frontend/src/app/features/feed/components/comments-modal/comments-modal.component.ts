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
  ViewChild,
  ElementRef,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CommentApiService } from '../../../../core/services/comment-api.service';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { Comment } from '../../../../core/models/comment.model';
import { PostFeedItem } from '../../../../core/models/post.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';
import { CommentTextComponent } from '../comment-text/comment-text.component';

type ReplyTarget = {
  rootCommentId: number;
  username: string;
};

@Component({
  selector: 'app-comments-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePipe, UserAvatarComponent, CommentTextComponent],
  templateUrl: './comments-modal.component.html',
})
export class CommentsModalComponent implements OnChanges {
  private readonly commentApi = inject(CommentApiService);
  private readonly currentUser = inject(CurrentUserService);

  @Input({ required: true }) post!: PostFeedItem;
  @Output() closed = new EventEmitter<void>();
  @Output() commentCreated = new EventEmitter<Comment>();
  @ViewChild('commentInput') private commentInput?: ElementRef<HTMLInputElement>;

  readonly comments = signal<Comment[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly newComment = signal('');
  readonly submitting = signal(false);
  readonly replyTarget = signal<ReplyTarget | null>(null);

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
    const target = this.replyTarget();
    this.commentApi
      .createComment(this.post.publicId, {
        texto,
        comentarioPadreId: target?.rootCommentId ?? null,
      })
      .subscribe({
        next: (created) => {
          if (created.comentarioPadreId) {
            this.comments.update((prev) =>
              prev.map((comment) =>
                comment.id === created.comentarioPadreId
                  ? { ...comment, respuestas: [...comment.respuestas, created] }
                  : comment,
              ),
            );
          } else {
            this.comments.update((prev) => [...prev, created]);
          }
          this.newComment.set('');
          this.replyTarget.set(null);
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

  replyTo(comment: Comment, rootComment?: Comment): void {
    const root = rootComment ?? comment;
    this.replyTarget.set({
      rootCommentId: root.id,
      username: comment.usuario.username,
    });
    this.newComment.set(`@${comment.usuario.username} `);
    queueMicrotask(() => {
      this.commentInput?.nativeElement.focus();
      const length = this.commentInput?.nativeElement.value.length ?? 0;
      this.commentInput?.nativeElement.setSelectionRange(length, length);
    });
  }

  cancelReply(): void {
    this.replyTarget.set(null);
  }

  onBackdropClick(): void {
    this.closed.emit();
  }
}

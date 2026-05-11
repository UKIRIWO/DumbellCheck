import { Component, Input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PostFeedItem } from '../../../../core/models/post.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';
import { CommentsModalComponent } from '../comments-modal/comments-modal.component';

@Component({
  selector: 'app-post-card',
  imports: [DatePipe, RouterLink, UserAvatarComponent, CommentsModalComponent],
  templateUrl: './post-card.component.html',
})
export class PostCardComponent {
  @Input({ required: true }) post!: PostFeedItem;
  currentSlide = 0;
  readonly commentsOpen = signal(false);

  get visibleEjercicios() {
    return this.post.ejercicios.slice(0, 5);
  }

  get remainingEjerciciosCount(): number {
    return Math.max(0, this.post.ejercicios.length - this.visibleEjercicios.length);
  }

  getSeriesCount(ejercicioId: number): number {
    const ejercicio = this.post.ejercicios.find((ej) => ej.id === ejercicioId);
    return ejercicio?.series.length ?? 0;
  }

  get avatarLetter(): string {
    return this.post.usuario.username.charAt(0).toUpperCase();
  }

  get hasMedia(): boolean {
    return !!this.post.multimediaUrl;
  }

  get totalSlides(): number {
    return this.hasMedia ? 2 : 1;
  }

  get isMediaVideo(): boolean {
    if (!this.post.multimediaUrl) return false;
    const lower = this.post.multimediaUrl.toLowerCase();
    return lower.endsWith('.mp4') || lower.endsWith('.webm') || lower.endsWith('.mov');
  }

  nextSlide(event: Event): void {
    event.stopPropagation();
    this.currentSlide = (this.currentSlide + 1) % this.totalSlides;
  }

  prevSlide(event: Event): void {
    event.stopPropagation();
    this.currentSlide = (this.currentSlide - 1 + this.totalSlides) % this.totalSlides;
  }

  blockPostNavigation(event: Event): void {
    event.stopPropagation();
  }

  openComments(event: Event): void {
    event.stopPropagation();
    this.commentsOpen.set(true);
  }

  closeComments(): void {
    this.commentsOpen.set(false);
  }

  onCommentCreated(): void {
    this.post.contadorComentarios = (this.post.contadorComentarios ?? 0) + 1;
  }
}

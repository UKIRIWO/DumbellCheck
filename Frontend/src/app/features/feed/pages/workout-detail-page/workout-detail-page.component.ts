import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PostApiService } from '../../../../core/services/post-api.service';
import { PostFeedItem } from '../../../../core/models/post.model';

@Component({
  selector: 'app-workout-detail-page',
  imports: [DatePipe, RouterLink],
  templateUrl: './workout-detail-page.component.html',
})
export class WorkoutDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly postApi = inject(PostApiService);

  post = signal<PostFeedItem | null>(null);
  loading = signal(true);
  errorMessage = signal('');
  currentSlide = 0;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const postId = Number(idParam);

    if (!Number.isInteger(postId) || postId <= 0) {
      this.loading.set(false);
      this.errorMessage.set('Publicación no válida.');
      return;
    }

    this.postApi.getPostById(postId).subscribe({
      next: (response) => {
        this.post.set(response);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('No se pudo cargar la publicación.');
      },
    });
  }

  get hasMedia(): boolean {
    return !!this.post()?.multimediaUrl;
  }

  get totalSlides(): number {
    return this.hasMedia ? 2 : 1;
  }

  get isMediaVideo(): boolean {
    const url = this.post()?.multimediaUrl;
    if (!url) return false;
    const lower = url.toLowerCase();
    return lower.endsWith('.mp4') || lower.endsWith('.webm') || lower.endsWith('.mov');
  }

  nextSlide(): void {
    this.currentSlide = (this.currentSlide + 1) % this.totalSlides;
  }

  prevSlide(): void {
    this.currentSlide = (this.currentSlide - 1 + this.totalSlides) % this.totalSlides;
  }
}

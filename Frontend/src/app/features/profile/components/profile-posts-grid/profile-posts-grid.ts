import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PostFeedItem } from '../../../../core/models/post.model';

@Component({
  selector: 'app-profile-posts-grid',
  imports: [RouterLink],
  templateUrl: './profile-posts-grid.html',
})
export class ProfilePostsGrid {
  @Input({ required: true }) posts!: PostFeedItem[];
  @Input() loading = false;
  @Input() hasMore = false;
  @Output() loadMore = new EventEmitter<void>();

  readonly MAX_EXERCISES = 5;

  isVideo(url: string): boolean {
    const lower = url.toLowerCase();
    return lower.includes('.mp4') || lower.includes('.webm') || lower.includes('.mov');
  }
}

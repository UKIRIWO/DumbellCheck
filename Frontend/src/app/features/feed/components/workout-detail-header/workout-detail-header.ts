import { Component, Input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PostFeedItem } from '../../../../core/models/post.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-workout-detail-header',
  imports: [DatePipe, RouterLink, UserAvatarComponent],
  templateUrl: './workout-detail-header.html',
  styleUrl: './workout-detail-header.scss',
})
export class WorkoutDetailHeader {
  @Input({ required: true }) post!: PostFeedItem;
}

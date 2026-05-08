import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-workout-detail-media',
  imports: [],
  templateUrl: './workout-detail-media.html',
  styleUrl: './workout-detail-media.scss',
})
export class WorkoutDetailMedia {
  @Input({ required: true }) multimediaUrl!: string;

  get isVideo(): boolean {
    const lower = this.multimediaUrl.toLowerCase();
    return lower.endsWith('.mp4') || lower.endsWith('.webm') || lower.endsWith('.mov');
  }
}

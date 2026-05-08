import { Component, Input } from '@angular/core';
import { EjercicioEnPost } from '../../../../core/models/post.model';

@Component({
  selector: 'app-workout-detail-exercises',
  imports: [],
  templateUrl: './workout-detail-exercises.html',
  styleUrl: './workout-detail-exercises.scss',
})
export class WorkoutDetailExercises {
  @Input({ required: true }) ejercicios!: EjercicioEnPost[];
}

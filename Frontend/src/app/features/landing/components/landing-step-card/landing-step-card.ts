import { Component, input } from '@angular/core';

@Component({
  selector: 'app-landing-step-card',
  imports: [],
  templateUrl: './landing-step-card.html',
  styleUrl: './landing-step-card.scss',
})
export class LandingStepCard {
  readonly step = input.required<string>();
  readonly title = input.required<string>();
  readonly description = input.required<string>();
}

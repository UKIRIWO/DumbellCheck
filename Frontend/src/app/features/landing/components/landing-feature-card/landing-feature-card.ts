import { Component, input } from '@angular/core';

@Component({
  selector: 'app-landing-feature-card',
  imports: [],
  templateUrl: './landing-feature-card.html',
  styleUrl: './landing-feature-card.scss',
})
export class LandingFeatureCard {
  readonly icon = input.required<string>();
  readonly title = input.required<string>();
  readonly description = input.required<string>();
}

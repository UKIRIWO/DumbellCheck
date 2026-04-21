import { Component } from '@angular/core';
import { LandingFeatureCard } from '../landing-feature-card/landing-feature-card';

@Component({
  selector: 'app-landing-features',
  imports: [LandingFeatureCard],
  templateUrl: './landing-features.html',
  styleUrl: './landing-features.scss',
})
export class LandingFeatures {}

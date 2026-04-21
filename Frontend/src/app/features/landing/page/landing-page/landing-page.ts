import { Component } from '@angular/core';
import { LandingHeader } from '../../components/landing-header/landing-header';
import { LandingHero } from '../../components/landing-hero/landing-hero';
import { LandingFeatures } from '../../components/landing-features/landing-features';
import { LandingHowItWorks } from '../../components/landing-how-it-works/landing-how-it-works';
import { LandingCta } from '../../components/landing-cta/landing-cta';
import { LandingFooter } from '../../components/landing-footer/landing-footer';

@Component({
  selector: 'app-landing-page',
  imports: [LandingHeader, LandingHero, LandingFeatures, LandingHowItWorks, LandingCta, LandingFooter],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.scss',
})
export class LandingPage {}

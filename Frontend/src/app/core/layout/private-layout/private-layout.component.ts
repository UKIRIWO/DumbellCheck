import { Component, computed, inject } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { BottomNavComponent } from '../../../shared/components/bottom-nav/bottom-nav.component';
import { SideNavComponent } from '../../../shared/components/side-nav/side-nav.component';
import { FeedRightSidebar } from '../../../features/feed/components/feed-right-sidebar/feed-right-sidebar';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-private-layout',
  standalone: true,
  imports: [RouterOutlet, BottomNavComponent, SideNavComponent, FeedRightSidebar],
  templateUrl: './private-layout.component.html',
})
export class PrivateLayoutComponent {
  private readonly router = inject(Router);

  private readonly url = toSignal(
    this.router.events.pipe(map(() => this.router.url)),
    { initialValue: this.router.url },
  );

  readonly hideSidebar = computed(() => this.url().startsWith('/app/profile'));
}

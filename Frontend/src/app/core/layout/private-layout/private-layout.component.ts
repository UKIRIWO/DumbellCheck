import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BottomNavComponent } from '../../../shared/components/bottom-nav/bottom-nav.component';
import { SideNavComponent } from '../../../shared/components/side-nav/side-nav.component';
import { FeedRightSidebar } from '../../../features/feed/components/feed-right-sidebar/feed-right-sidebar';

@Component({
  selector: 'app-private-layout',
  standalone: true,
  imports: [RouterOutlet, BottomNavComponent, SideNavComponent, FeedRightSidebar],
  templateUrl: './private-layout.component.html',
})
export class PrivateLayoutComponent {}

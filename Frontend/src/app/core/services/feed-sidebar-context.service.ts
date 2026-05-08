import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class FeedSidebarContextService {
  readonly highlightedUsername = signal<string | null>(null);

  setHighlightedAuthor(username: string | null): void {
    this.highlightedUsername.set(username);
  }
}

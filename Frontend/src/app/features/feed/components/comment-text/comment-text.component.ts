import { Component, EventEmitter, Output, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

type Segment =
  | { kind: 'text'; text: string }
  | { kind: 'mention'; username: string };

const MENTION_REGEX = /@([A-Za-z0-9_]{1,50})/g;

@Component({
  selector: 'app-comment-text',
  standalone: true,
  imports: [RouterLink],
  template: `
    @for (segment of segments(); track $index) {
      @if (segment.kind === 'mention') {
        <a
          [routerLink]="['/app/profile', segment.username]"
          (click)="mentionClick.emit()"
          class="text-brand-blue hover:underline"
        >&#64;{{ segment.username }}</a>
      } @else {
        <span>{{ segment.text }}</span>
      }
    }
  `,
})
export class CommentTextComponent {
  readonly texto = input.required<string>();
  readonly mencionesValidas = input<string[]>([]);
  @Output() mentionClick = new EventEmitter<void>();

  readonly segments = computed<Segment[]>(() => {
    const text = this.texto() ?? '';
    const validByLower = new Map<string, string>(
      (this.mencionesValidas() ?? []).map((u) => [u.toLowerCase(), u]),
    );

    const segments: Segment[] = [];
    let lastIndex = 0;

    for (const match of text.matchAll(MENTION_REGEX)) {
      const start = match.index ?? 0;
      const fullMatch = match[0];
      const mentionLower = match[1].toLowerCase();
      const realUsername = validByLower.get(mentionLower);

      if (start > lastIndex) {
        segments.push({ kind: 'text', text: text.slice(lastIndex, start) });
      }

      if (realUsername) {
        segments.push({ kind: 'mention', username: realUsername });
      } else {
        segments.push({ kind: 'text', text: fullMatch });
      }

      lastIndex = start + fullMatch.length;
    }

    if (lastIndex < text.length) {
      segments.push({ kind: 'text', text: text.slice(lastIndex) });
    }

    return segments;
  });
}

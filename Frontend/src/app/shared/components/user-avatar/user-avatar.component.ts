import { Component, computed, input } from '@angular/core';

type AvatarSize = 'sm' | 'md' | 'lg' | 'xl';

const AVATAR_BG_CLASSES: readonly string[] = [
  'bg-red-500',
  'bg-orange-500',
  'bg-amber-500',
  'bg-yellow-500',
  'bg-lime-500',
  'bg-green-500',
  'bg-emerald-500',
  'bg-teal-500',
  'bg-cyan-500',
  'bg-sky-600',
  'bg-blue-500',
  'bg-indigo-500',
  'bg-violet-500',
  'bg-purple-500',
  'bg-fuchsia-500',
  'bg-pink-500',
  'bg-rose-500',
  'bg-stone-500',
  'bg-slate-600',
  'bg-zinc-600',
];

@Component({
  selector: 'app-user-avatar',
  standalone: true,
  template: `
    <div
      class="flex shrink-0 items-center justify-center overflow-hidden rounded-full text-white font-heading font-bold"
      [class]="containerClass()"
    >
      @if (fotoPerfilUrl()) {
        <img
          [src]="fotoPerfilUrl()!"
          [alt]="username()"
          class="h-full w-full object-cover"
        />
      } @else {
        <span>{{ initial() }}</span>
      }
    </div>
  `,
})
export class UserAvatarComponent {
  readonly username = input.required<string>();
  readonly fotoPerfilUrl = input<string | null | undefined>(null);
  readonly size = input<AvatarSize>('md');

  readonly initial = computed(() => {
    const u = this.username();
    return u && u.length > 0 ? u.charAt(0).toUpperCase() : 'U';
  });

  readonly sizeClass = computed(() => {
    switch (this.size()) {
      case 'sm':
        return 'h-9 w-9 text-sm';
      case 'md':
        return 'h-10 w-10 text-sm';
      case 'lg':
        return 'h-12 w-12 text-base';
      case 'xl':
        return 'h-20 w-20 text-3xl';
    }
  });

  readonly bgClass = computed(() => this.pickColor(this.username()));

  readonly containerClass = computed(() => `${this.sizeClass()} ${this.bgClass()}`);

  private pickColor(name: string): string {
    if (!name) {
      return AVATAR_BG_CLASSES[0];
    }
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash) % AVATAR_BG_CLASSES.length;
    return AVATAR_BG_CLASSES[index];
  }
}

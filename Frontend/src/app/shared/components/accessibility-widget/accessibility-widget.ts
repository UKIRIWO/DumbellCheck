import { Component, ElementRef, HostListener, inject, signal } from '@angular/core';
import {
  AccessibilityOptionKey,
  AccessibilityService,
} from '../../../core/services/accessibility.service';

interface AccessibilityOption {
  key: AccessibilityOptionKey;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-accessibility-widget',
  imports: [],
  templateUrl: './accessibility-widget.html',
  styleUrl: './accessibility-widget.scss',
})
export class AccessibilityWidget {
  protected readonly a11y = inject(AccessibilityService);
  private readonly host = inject(ElementRef<HTMLElement>);

  readonly panelOpen = signal(false);
  readonly state = this.a11y.state;

  readonly optionKeys: AccessibilityOption[] = [
    { key: 'resaltar-enfoque', label: 'Resaltar enfoque', icon: 'mdi-focus-field' },
    { key: 'espaciado-lineas', label: 'Espaciado de líneas', icon: 'mdi-format-line-spacing' },
    { key: 'resaltar-enlaces', label: 'Resaltar enlaces', icon: 'mdi-link-variant' },
  ];

  togglePanel(event: MouseEvent): void {
    event.stopPropagation();
    this.panelOpen.update((open) => !open);
  }

  closePanel(): void {
    this.panelOpen.set(false);
  }

  onOptionChange(key: AccessibilityOptionKey, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.a11y.setOption(key, checked);
  }

  isOptionChecked(key: AccessibilityOptionKey): boolean {
    return this.state().opciones[key];
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.panelOpen()) {
      return;
    }
    const root = this.host.nativeElement;
    if (!root.contains(event.target as Node)) {
      this.closePanel();
    }
  }
}

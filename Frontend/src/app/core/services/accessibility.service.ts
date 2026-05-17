import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';

export type AccessibilityOptionKey =
  | 'resaltar-enfoque'
  | 'espaciado-lineas'
  | 'resaltar-enlaces';

export interface AccessibilityOptions {
  'resaltar-enfoque': boolean;
  'espaciado-lineas': boolean;
  'resaltar-enlaces': boolean;
}

export interface AccessibilityState {
  tamano: number;
  opciones: AccessibilityOptions;
}

const STORAGE_KEY = 'accesibilidad-estadoActual';
const STYLE_ID = 'estilos-accesibilidad';

const DEFAULT_STATE: AccessibilityState = {
  tamano: 100,
  opciones: {
    'resaltar-enfoque': false,
    'espaciado-lineas': false,
    'resaltar-enlaces': false,
  },
};

@Injectable({ providedIn: 'root' })
export class AccessibilityService {
  private readonly document = inject(DOCUMENT);

  readonly state = signal<AccessibilityState>(this.loadState());

  constructor() {
    this.applyStyles();
  }

  increaseFontSize(): void {
    this.patchState((s) => (s.tamano < 150 ? { ...s, tamano: s.tamano + 10 } : s));
  }

  decreaseFontSize(): void {
    this.patchState((s) => (s.tamano > 50 ? { ...s, tamano: s.tamano - 10 } : s));
  }

  resetFontSize(): void {
    this.patchState((s) => ({ ...s, tamano: 100 }));
  }

  setOption(key: AccessibilityOptionKey, enabled: boolean): void {
    this.patchState((s) => ({
      ...s,
      opciones: { ...s.opciones, [key]: enabled },
    }));
  }

  resetAll(): void {
    this.state.set({ ...DEFAULT_STATE, opciones: { ...DEFAULT_STATE.opciones } });
    this.persistAndApply();
  }

  private patchState(updater: (state: AccessibilityState) => AccessibilityState): void {
    const next = updater(this.state());
    if (next !== this.state()) {
      this.state.set(next);
      this.persistAndApply();
    }
  }

  private persistAndApply(): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.state()));
    this.applyStyles();
  }

  private loadState(): AccessibilityState {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return { ...DEFAULT_STATE, opciones: { ...DEFAULT_STATE.opciones } };
      }
      const parsed = JSON.parse(raw) as Partial<AccessibilityState> & { tamaño?: number };
      return {
        tamano: parsed.tamano ?? parsed['tamaño'] ?? 100,
        opciones: {
          ...DEFAULT_STATE.opciones,
          'resaltar-enfoque': parsed.opciones?.['resaltar-enfoque'] ?? false,
          'espaciado-lineas': parsed.opciones?.['espaciado-lineas'] ?? false,
          'resaltar-enlaces': parsed.opciones?.['resaltar-enlaces'] ?? false,
        },
      };
    } catch {
      return { ...DEFAULT_STATE, opciones: { ...DEFAULT_STATE.opciones } };
    }
  }

  private applyStyles(): void {
    const { tamano, opciones } = this.state();
    let css = `html { font-size: ${tamano}% !important; }`;

    if (opciones['resaltar-enfoque']) {
      css += `
        *:focus {
          outline: 3px solid #ff9900 !important;
          outline-offset: 2px !important;
        }
      `;
    }

    if (opciones['espaciado-lineas']) {
      css += `* { line-height: 1.8 !important; }`;
    }

    if (opciones['resaltar-enlaces']) {
      css += `
        a {
          font-weight: bold !important;
        }
        a:hover {
          text-decoration: underline !important;
          background-color: #fcfc5d !important;
          color: #000 !important;
        }
      `;
    }

    let styleEl = this.document.getElementById(STYLE_ID) as HTMLStyleElement | null;
    if (!styleEl) {
      styleEl = this.document.createElement('style');
      styleEl.id = STYLE_ID;
      this.document.head.appendChild(styleEl);
    }
    styleEl.textContent = css;
  }
}

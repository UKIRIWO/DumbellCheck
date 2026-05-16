import { Component, Input, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';


@Component({
  selector: 'app-password-field',
  imports: [ReactiveFormsModule],
  templateUrl: './password-field.html',
  styleUrl: './password-field.scss',
})
export class PasswordField {
  @Input({ required: true }) control!: FormControl<string>;
  @Input() placeholder = '';
  @Input() autocomplete: string = 'current-password';
  @Input() inputId = '';

  @Input() size: 'md' | 'sm' = 'md';

  readonly visible = signal(false);


  readonly fieldBaseClass =
    'w-full rounded-[10px] border-2 border-[#e8edf2] bg-[#fafbfc] text-charcoal outline-none transition focus:border-sky focus:bg-white disabled:opacity-60';

  readonly sizeMdClass = 'px-3.5 py-3 pr-11 text-[0.95rem]';
  readonly sizeSmClass = 'px-[13px] py-[11px] pr-11 text-[0.92rem]';

  toggle(): void {
    this.visible.update((v) => !v);
  }
}

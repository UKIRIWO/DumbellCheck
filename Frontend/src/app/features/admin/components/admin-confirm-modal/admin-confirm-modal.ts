import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-admin-confirm-modal',
  imports: [],
  templateUrl: './admin-confirm-modal.html',
})
export class AdminConfirmModal {
  @Input() title = '¿Confirmar acción?';
  @Input() message = 'Esta acción no se puede deshacer.';
  @Input() confirmLabel = 'Confirmar';
  @Input() loading = false;
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();
}

import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ChatMessage } from '../../../../core/models/chat.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-chat-message-item',
  imports: [DatePipe, RouterLink, FormsModule, UserAvatarComponent],
  templateUrl: './chat-message-item.html',
})
export class ChatMessageItem {
  @Input({ required: true }) message!: ChatMessage;
  @Input() showAvatar = true;
  @Output() deleteClick = new EventEmitter<ChatMessage>();
  @Output() editSave = new EventEmitter<{ message: ChatMessage; content: string }>();
  @Output() replyClick = new EventEmitter<ChatMessage>();

  readonly isEditing = signal(false);
  readonly editContent = signal('');

  get isImage(): boolean {
    return this.message.tipoMensaje === 'imagen';
  }

  get isVideo(): boolean {
    return this.message.tipoMensaje === 'video';
  }

  get isRoutine(): boolean {
    return this.message.tipoMensaje === 'rutina';
  }

  startEdit(): void {
    this.editContent.set(this.message.contenido ?? '');
    this.isEditing.set(true);
  }

  cancelEdit(): void {
    this.isEditing.set(false);
  }

  saveEdit(): void {
    const content = this.editContent().trim();
    if (!content) return;
    this.editSave.emit({ message: this.message, content });
    this.isEditing.set(false);
  }

  onEditKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.saveEdit();
    }
    if (event.key === 'Escape') this.cancelEdit();
  }
}

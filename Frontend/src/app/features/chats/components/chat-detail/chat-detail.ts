import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ChatApiService } from '../../../../core/services/chat-api.service';
import { ChatDetail as ChatDetailModel, ChatMessage } from '../../../../core/models/chat.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';
import { ChatMessageItem } from '../chat-message-item/chat-message-item';
import { ChatInfoModal } from '../chat-info-modal/chat-info-modal';
import { ChatRoutinePickerModal } from '../chat-routine-picker-modal/chat-routine-picker-modal';

interface ReplyTarget {
  id: number;
  username: string;
  preview: string;
}

@Component({
  selector: 'app-chat-detail',
  imports: [FormsModule, UserAvatarComponent, ChatMessageItem, ChatInfoModal, ChatRoutinePickerModal],
  templateUrl: './chat-detail.html',
})
export class ChatDetail implements OnChanges, OnDestroy {
  private readonly chatApi = inject(ChatApiService);

  @Input({ required: true }) chat!: ChatDetailModel;
  @Input() myUsername = '';
  @Output() chatUpdated = new EventEmitter<void>();
  @Output() chatDetailUpdated = new EventEmitter<ChatDetailModel>();
  @Output() leftChat = new EventEmitter<void>();

  @ViewChild('messagesContainer') private messagesContainer?: ElementRef<HTMLElement>;
  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;

  readonly messages = signal<ChatMessage[]>([]);
  readonly loading = signal(false);
  readonly loadingOlder = signal(false);
  readonly hasMore = signal(false);
  readonly nextCursor = signal<number | null>(null);
  readonly errorMessage = signal('');
  readonly newMessage = signal('');
  readonly sending = signal(false);
  readonly replyTarget = signal<ReplyTarget | null>(null);
  readonly showRoutinePicker = signal(false);
  readonly mediaPreviewUrl = signal<string | null>(null);
  readonly mediaFile = signal<File | null>(null);
  readonly mediaType = signal<'image' | 'video' | null>(null);
  readonly showInfoModal = signal(false);
  readonly showAttachMenu = signal(false);

  private pollSub?: Subscription;
  private lastSeenMessageId: number | null = null;

  get title(): string {
    return this.chat?.nombre ?? '';
  }

  get subtitleLine(): string {
    if (!this.chat) return '';
    if (this.chat.esGrupo) return `${this.chat.participantes.length} miembros`;
    const other = this.chat.participantes.find((p) => p.username !== this.myUsername);
    return other?.nombre ?? '';
  }

  get fotoUrl(): string | null {
    if (this.chat?.fotoUrl) return this.chat.fotoUrl;
    if (!this.chat?.esGrupo) {
      const other = this.chat?.participantes.find((p) => p.username !== this.myUsername);
      return other?.fotoPerfilUrl ?? null;
    }
    return null;
  }

  get otherUsername(): string {
    if (this.chat?.esGrupo) return this.chat.nombre ?? '';
    const other = this.chat?.participantes.find((p) => p.username !== this.myUsername);
    return other?.username ?? '';
  }

  get hasMediaAttached(): boolean {
    return this.mediaFile() !== null;
  }

  get textInputDisabled(): boolean {
    return this.sending() || this.hasMediaAttached;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chat'] && this.chat) {
      this.resetAndLoad();
    }
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  private resetAndLoad(): void {
    this.pollSub?.unsubscribe();
    this.messages.set([]);
    this.nextCursor.set(null);
    this.hasMore.set(false);
    this.lastSeenMessageId = null;
    this.loadMessages(true);
    this.startPolling();
  }

  loadMessages(initial = false): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.chatApi.getMensajes(this.chat.publicId, null, 30).subscribe({
      next: (page) => {
        this.messages.set(page.content);
        this.hasMore.set(page.hasMore);
        this.nextCursor.set(page.nextCursor);
        this.loading.set(false);
        if (initial) {
          setTimeout(() => this.scrollToBottom(), 50);
        }
        this.markSeen();
      },
      error: () => { this.errorMessage.set('Error cargando mensajes.'); this.loading.set(false); },
    });
  }

  loadOlderMessages(): void {
    if (this.loadingOlder() || !this.hasMore()) return;
    this.loadingOlder.set(true);
    this.chatApi.getMensajes(this.chat.publicId, this.nextCursor(), 30).subscribe({
      next: (page) => {
        this.messages.update((prev) => [...page.content, ...prev]);
        this.hasMore.set(page.hasMore);
        this.nextCursor.set(page.nextCursor);
        this.loadingOlder.set(false);
      },
      error: () => this.loadingOlder.set(false),
    });
  }

  private startPolling(): void {
    this.pollSub = interval(5000).pipe(
      switchMap(() => this.chatApi.getMensajes(this.chat.publicId, null, 30)),
    ).subscribe({
      next: (page) => {
        const current = this.messages();
        const currentIds = new Set(current.map((m) => m.id));
        const newMsgs = page.content.filter((m) => !currentIds.has(m.id));
        if (newMsgs.length > 0) {
          this.messages.update((prev) => [...prev, ...newMsgs]);
          setTimeout(() => this.scrollToBottom(), 50);
          this.markSeen();
          this.chatUpdated.emit();
        }

        this.messages.update((prev) =>
          prev.map((m) => page.content.find((u) => u.id === m.id) ?? m),
        );
      },
    });
  }

  private markSeen(): void {
    this.chatApi.updateLastSeen(this.chat.publicId).subscribe();
  }

  private scrollToBottom(): void {
    const el = this.messagesContainer?.nativeElement;
    if (el) el.scrollTop = el.scrollHeight;
  }

  send(): void {
    const file = this.mediaFile();
    if (file) {
      this.sendMedia(file);
      return;
    }
    const text = this.newMessage().trim();
    if (!text || this.sending()) return;
    this.sendText(text);
  }

  private sendText(text: string): void {
    this.sending.set(true);
    const reply = this.replyTarget();
    this.chatApi.sendMessage(this.chat.publicId, {
      tipoMensaje: 'texto',
      contenido: text,
      mensajeReferenciaId: reply?.id,
    }).subscribe({
      next: (msg) => {
        this.messages.update((prev) => [...prev, msg]);
        this.newMessage.set('');
        this.replyTarget.set(null);
        this.sending.set(false);
        setTimeout(() => this.scrollToBottom(), 50);
        this.markSeen();
        this.chatUpdated.emit();
      },
      error: () => { this.errorMessage.set('Error enviando mensaje.'); this.sending.set(false); },
    });
  }

  private sendMedia(file: File): void {
    this.sending.set(true);
    const tipo = file.type.startsWith('video/') ? 'video' : 'imagen';
    const reply = this.replyTarget();
    this.chatApi.uploadMedia(this.chat.publicId, file).pipe(
      switchMap((url) => this.chatApi.sendMessage(this.chat.publicId, {
        tipoMensaje: tipo,
        archivoUrl: url,
        mensajeReferenciaId: reply?.id,
      })),
    ).subscribe({
      next: (msg) => {
        this.messages.update((prev) => [...prev, msg]);
        this.clearMedia();
        this.replyTarget.set(null);
        this.sending.set(false);
        setTimeout(() => this.scrollToBottom(), 50);
        this.markSeen();
        this.chatUpdated.emit();
      },
      error: () => { this.errorMessage.set('Error enviando archivo.'); this.sending.set(false); },
    });
  }

  sendRoutine(routineId: number): void {
    this.showRoutinePicker.set(false);
    this.sending.set(true);
    this.chatApi.sendMessage(this.chat.publicId, {
      tipoMensaje: 'rutina',
      rutinaId: routineId,
    }).subscribe({
      next: (msg) => {
        this.messages.update((prev) => [...prev, msg]);
        this.sending.set(false);
        setTimeout(() => this.scrollToBottom(), 50);
        this.markSeen();
        this.chatUpdated.emit();
      },
      error: () => { this.errorMessage.set('Error enviando rutina.'); this.sending.set(false); },
    });
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  toggleAttachMenu(): void {
    this.showAttachMenu.update((v) => !v);
  }

  closeAttachMenu(): void {
    this.showAttachMenu.set(false);
  }

  openMediaPicker(): void {
    this.closeAttachMenu();
    this.fileInput?.nativeElement.click();
  }

  openRoutinePicker(): void {
    this.closeAttachMenu();
    this.showRoutinePicker.set(true);
  }

  closeRoutinePicker(): void {
    this.showRoutinePicker.set(false);
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.closeAttachMenu();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.newMessage.set('');
    this.mediaFile.set(file);
    const url = URL.createObjectURL(file);
    this.mediaPreviewUrl.set(url);
    this.mediaType.set(file.type.startsWith('video/') ? 'video' : 'image');
    input.value = '';
  }

  clearMedia(): void {
    this.mediaFile.set(null);
    if (this.mediaPreviewUrl()) {
      URL.revokeObjectURL(this.mediaPreviewUrl()!);
      this.mediaPreviewUrl.set(null);
    }
    this.mediaType.set(null);
  }

  onEditSave(event: { message: ChatMessage; content: string }): void {
    this.chatApi.editMessage(this.chat.publicId, event.message.id, { contenido: event.content }).subscribe({
      next: (updated) => {
        this.messages.update((prev) => prev.map((m) => m.id === updated.id ? updated : m));
      },
      error: () => this.errorMessage.set('Error editando mensaje.'),
    });
  }

  onDelete(message: ChatMessage): void {
    this.chatApi.deleteMessage(this.chat.publicId, message.id).subscribe({
      next: () => {
        this.messages.update((prev) =>
          prev.map((m) => m.id === message.id ? { ...m, eliminado: true, contenido: undefined } : m),
        );
      },
      error: () => this.errorMessage.set('Error eliminando mensaje.'),
    });
  }

  setReply(message: ChatMessage): void {
    const preview = message.contenido
      ? (message.contenido.length > 50 ? message.contenido.slice(0, 50) + '…' : message.contenido)
      : `[${message.tipoMensaje}]`;
    this.replyTarget.set({ id: message.id, username: message.username, preview });
  }

  cancelReply(): void { this.replyTarget.set(null); }

  shouldShowAvatar(messages: ChatMessage[], index: number): boolean {
    if (index === 0) return true;
    const prev = messages[index - 1];
    const cur = messages[index];
    return prev.usuarioId !== cur.usuarioId;
  }

  openInfoModal(): void {
    this.showInfoModal.set(true);
  }

  closeInfoModal(): void {
    this.showInfoModal.set(false);
  }

  onChatInfoUpdated(detail: ChatDetailModel): void {
    this.chat = detail;
    this.chatDetailUpdated.emit(detail);
    this.chatUpdated.emit();
  }

  onChatLeft(): void {
    this.closeInfoModal();
    this.leftChat.emit();
  }

  exitChat(): void {
    this.leftChat.emit();
  }
}

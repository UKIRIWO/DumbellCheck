import { Component, Input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ChatListItem as ChatListItemModel } from '../../../../core/models/chat.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-chat-list-item',
  imports: [DatePipe, UserAvatarComponent],
  templateUrl: './chat-list-item.html',
})
export class ChatListItem {
  @Input({ required: true }) chat!: ChatListItemModel;
  @Input() active = false;
  @Input() myUsername = '';

  get displayName(): string {
    return this.chat.nombre;
  }

  get esGrupo(): boolean {
    return this.chat.tipo === 'grupo';
  }

  get fotoUrl(): string | null {
    if (this.chat.fotoUrl) return this.chat.fotoUrl;
    if (!this.esGrupo) {
      const other = this.chat.participantes.find((p) => p.username !== this.myUsername);
      return other?.fotoPerfilUrl ?? null;
    }
    return null;
  }

  get avatarUsername(): string {
    if (this.esGrupo) return this.chat.nombre ?? '';
    const other = this.chat.participantes.find((p) => p.username !== this.myUsername);
    return other?.username ?? '';
  }

  get lastMessagePreview(): string {
    const m = this.chat.ultimoMensaje;
    if (!m) return '';
    const prefix = m.esMio ? 'Tú: ' : `${m.remitenteUsername}: `;
    const text = m.contenido?.trim() || this.tipoLabel(m.tipoMensaje);
    return text ? `${prefix}${text}` : '';
  }

  tipoLabel(tipo: string): string {
    const map: Record<string, string> = {
      texto: 'Mensaje',
      imagen: 'Imagen',
      video: 'Vídeo',
      rutina: 'Rutina',
    };
    return map[tipo] ?? tipo;
  }
}

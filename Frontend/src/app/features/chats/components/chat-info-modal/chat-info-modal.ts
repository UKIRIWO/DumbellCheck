import { Component, EventEmitter, Input, OnChanges, OnInit, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { ChatApiService } from '../../../../core/services/chat-api.service';
import { ChatDetail, ChatParticipant, ChatSearchUser } from '../../../../core/models/chat.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-chat-info-modal',
  imports: [FormsModule, UserAvatarComponent],
  templateUrl: './chat-info-modal.html',
})
export class ChatInfoModal implements OnChanges, OnInit {
  private readonly chatApi = inject(ChatApiService);

  @Input({ required: true }) chat!: ChatDetail;
  @Input() myUsername = '';
  @Output() closed = new EventEmitter<void>();
  @Output() updated = new EventEmitter<ChatDetail>();
  @Output() chatLeft = new EventEmitter<void>();

  readonly groupName = signal('');
  readonly savingName = signal(false);
  readonly leaving = signal(false);
  readonly showLeaveConfirm = signal(false);
  readonly showAddMembers = signal(false);
  readonly roleLoadingById = signal<Record<number, boolean>>({});
  readonly errorMessage = signal('');

  readonly uploadingPhoto = signal(false);
  readonly photoPreview = signal<string | null>(null);

  readonly addSearchQuery = signal('');
  readonly addUsers = signal<ChatSearchUser[]>([]);
  readonly addSelected = signal<ChatSearchUser[]>([]);
  readonly addLoading = signal(false);
  readonly addingMembers = signal(false);

  private readonly addSearch$ = new Subject<string>();

  ngOnInit(): void {
    this.addSearch$.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap((q) => {
        this.addLoading.set(true);
        return this.chatApi.searchUsers(q || undefined);
      }),
    ).subscribe({
      next: (users) => {
        this.addUsers.set(this.filterAddableUsers(users));
        this.addLoading.set(false);
      },
      error: () => this.addLoading.set(false),
    });
  }

  ngOnChanges(): void {
    if (this.chat) {
      this.groupName.set(this.chat.nombre ?? '');
      this.photoPreview.set(this.chat.fotoUrl ?? null);
      this.errorMessage.set('');
    }
  }

  get canManage(): boolean {
    return this.chat.soyAdmin;
  }

  get canAddMembers(): boolean {
    return this.chat.esGrupo && this.canManage && this.chat.participantes.length < 10;
  }

  get maxSelectableToAdd(): number {
    return Math.max(0, 10 - this.chat.participantes.length);
  }

  get leaveLabel(): string {
    return this.chat.esGrupo ? 'Salir del grupo' : 'Abandonar chat';
  }

  get leaveConfirmMessage(): string {
    return this.chat.esGrupo
      ? 'Dejarás de ver este grupo y dejarás de recibir sus mensajes. Los demás miembros seguirán en el chat.'
      : 'Dejarás de ver esta conversación. El otro usuario podrá seguir escribiendo, pero ya no aparecerá en tu lista.';
  }

  openAddMembers(): void {
    this.errorMessage.set('');
    this.addSelected.set([]);
    this.addSearchQuery.set('');
    this.showAddMembers.set(true);
    this.addSearch$.next('');
  }

  closeAddMembers(): void {
    this.showAddMembers.set(false);
    this.addSelected.set([]);
  }

  onAddQueryChange(q: string): void {
    this.addSearchQuery.set(q);
    this.addSearch$.next(q);
  }

  isAddSelected(user: ChatSearchUser): boolean {
    return this.addSelected().some((s) => s.id === user.id);
  }

  isAlreadyMember(user: ChatSearchUser): boolean {
    return this.chat.participantes.some((p) => p.username === user.username);
  }

  toggleAddUser(user: ChatSearchUser): void {
    if (this.isAlreadyMember(user)) return;
    if (this.isAddSelected(user)) {
      this.addSelected.update((s) => s.filter((x) => x.id !== user.id));
    } else if (this.addSelected().length < this.maxSelectableToAdd) {
      this.addSelected.update((s) => [...s, user]);
    }
  }

  confirmAddMembers(): void {
    const sel = this.addSelected();
    if (sel.length === 0 || this.addingMembers()) return;
    this.addingMembers.set(true);
    this.errorMessage.set('');

    this.chatApi.addGroupMembers(this.chat.publicId, { usernames: sel.map((u) => u.username) }).subscribe({
      next: (detail) => {
        this.addingMembers.set(false);
        this.showAddMembers.set(false);
        this.addSelected.set([]);
        this.updated.emit(detail);
      },
      error: (err: { error?: { error?: string } }) => {
        const msg = err?.error?.error;
        this.errorMessage.set(msg && msg.length > 0 ? msg : 'No se pudieron añadir los miembros.');
        this.addingMembers.set(false);
      },
    });
  }

  onGroupPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !this.chat.esGrupo || !this.canManage) return;
    this.uploadingPhoto.set(true);
    this.errorMessage.set('');
    this.chatApi.uploadGroupPhoto(this.chat.publicId, file).subscribe({
      next: (detail) => {
        this.photoPreview.set(detail.fotoUrl ?? null);
        this.uploadingPhoto.set(false);
        this.updated.emit(detail);
      },
      error: (err: { error?: { error?: string } }) => {
        const msg = err?.error?.error;
        this.errorMessage.set(msg && msg.length > 0 ? msg : 'No se pudo subir la foto del grupo.');
        this.uploadingPhoto.set(false);
      },
    });
    input.value = '';
  }

  openLeaveConfirm(): void {
    this.errorMessage.set('');
    this.showLeaveConfirm.set(true);
  }

  cancelLeaveConfirm(): void {
    this.showLeaveConfirm.set(false);
  }

  confirmLeave(): void {
    if (this.leaving()) return;
    this.leaving.set(true);
    this.errorMessage.set('');
    this.chatApi.leaveChat(this.chat.publicId).subscribe({
      next: () => {
        this.leaving.set(false);
        this.showLeaveConfirm.set(false);
        this.chatLeft.emit();
      },
      error: (err: { error?: { error?: string } }) => {
        const msg = err?.error?.error;
        this.errorMessage.set(msg && msg.length > 0 ? msg : 'No se pudo abandonar el chat.');
        this.leaving.set(false);
        this.showLeaveConfirm.set(false);
      },
    });
  }

  saveGroupName(): void {
    if (!this.chat.esGrupo || !this.canManage || this.savingName()) return;
    this.savingName.set(true);
    this.errorMessage.set('');
    this.chatApi.updateChat(this.chat.publicId, { nombre: this.groupName().trim() || undefined }).subscribe({
      next: (detail) => {
        this.savingName.set(false);
        this.updated.emit(detail);
      },
      error: () => {
        this.errorMessage.set('No se pudo actualizar el nombre del grupo.');
        this.savingName.set(false);
      },
    });
  }

  isSelf(participant: ChatParticipant): boolean {
    return participant.username === this.myUsername;
  }

  canChangeRole(participant: ChatParticipant): boolean {
    return this.canManage && !this.isSelf(participant);
  }

  onRoleChange(participant: ChatParticipant, rol: string): void {
    if (!this.canChangeRole(participant) || rol === participant.rol) return;
    const nuevoRol = rol as 'admin' | 'miembro';
    if (this.isSelf(participant) && nuevoRol === 'miembro') {
      this.errorMessage.set('No puedes quitarte el rol de administrador a ti mismo.');
      return;
    }
    this.roleLoadingById.update((m) => ({ ...m, [participant.usuarioId]: true }));
    this.errorMessage.set('');
    this.chatApi.updateParticipantRole(this.chat.publicId, participant.usuarioId, { rol: nuevoRol }).subscribe({
      next: (detail) => {
        this.roleLoadingById.update((m) => ({ ...m, [participant.usuarioId]: false }));
        this.updated.emit(detail);
      },
      error: () => {
        this.errorMessage.set('No se pudo cambiar el rol del participante.');
        this.roleLoadingById.update((m) => ({ ...m, [participant.usuarioId]: false }));
      },
    });
  }

  isRoleLoading(usuarioId: number): boolean {
    return !!this.roleLoadingById()[usuarioId];
  }

  private filterAddableUsers(users: ChatSearchUser[]): ChatSearchUser[] {
    const memberUsernames = new Set(this.chat.participantes.map((p) => p.username.toLowerCase()));
    return users.filter((u) => !memberUsernames.has(u.username.toLowerCase()));
  }
}

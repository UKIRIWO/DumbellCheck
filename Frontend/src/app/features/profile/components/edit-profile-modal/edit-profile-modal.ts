import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Perfil, Plataforma, UsuarioEnlace } from '../../../../core/models/profile.model';
import { ProfileApiService } from '../../../../core/services/profile-api.service';
import { ProfileLinkIcon } from '../profile-link-icon/profile-link-icon';

@Component({
  selector: 'app-edit-profile-modal',
  imports: [FormsModule, ProfileLinkIcon],
  templateUrl: './edit-profile-modal.html',
})
export class EditProfileModal implements OnInit {
  @Input({ required: true }) perfil!: Perfil;
  @Output() closed = new EventEmitter<Perfil>();

  private readonly profileApi = inject(ProfileApiService);

  readonly plataformas: Plataforma[] = ['instagram', 'youtube', 'twitter', 'tiktok', 'web', 'otro'];

  nombre = '';
  apellido1 = '';
  apellido2 = '';
  biografia = '';

  enlaces = signal<UsuarioEnlace[]>([]);
  newLinkPlataforma: Plataforma = 'instagram';
  newLinkUrl = '';

  savingProfile = signal(false);
  uploadingFoto = signal(false);
  uploadingBanner = signal(false);
  addingLink = signal(false);
  deletingLinkId = signal<number | null>(null);
  profileError = signal('');
  linkError = signal('');

  fotoPreview = signal<string | null>(null);
  bannerPreview = signal<string | null>(null);

  get canAddLink(): boolean {
    return (
      this.enlaces().length < 10 &&
      this.newLinkUrl.trim().length > 0 &&
      !this.enlaces().some((e) => e.url === this.newLinkUrl.trim())
    );
  }

  ngOnInit(): void {
    this.nombre = this.perfil.nombre;
    this.apellido1 = this.perfil.apellido1;
    this.apellido2 = this.perfil.apellido2 ?? '';
    this.biografia = this.perfil.biografia ?? '';
    this.enlaces.set([...this.perfil.enlaces]);
    this.fotoPreview.set(this.perfil.fotoPerfilUrl ?? null);
    this.bannerPreview.set(this.perfil.bannerUrl ?? null);
  }

  saveProfile(): void {
    if (this.savingProfile()) return;
    this.profileError.set('');
    this.savingProfile.set(true);

    this.profileApi
      .updateMyProfile({
        nombre: this.nombre.trim(),
        apellido1: this.apellido1.trim(),
        apellido2: this.apellido2.trim() || undefined,
        biografia: this.biografia.trim() || undefined,
      })
      .subscribe({
        next: (updated) => {
          this.savingProfile.set(false);
          this.closed.emit(updated);
        },
        error: (err) => {
          this.savingProfile.set(false);
          this.profileError.set((err?.error?.error as string | undefined) ?? 'No se pudo guardar el perfil.');
        },
      });
  }

  onFotoSelected(event: Event): void {
    const file = this.extractFile(event);
    if (!file) return;
    this.uploadingFoto.set(true);
    this.profileApi.uploadFoto(file).subscribe({
      next: (url) => {
        this.fotoPreview.set(url);
        this.uploadingFoto.set(false);
      },
      error: (err) => {
        this.uploadingFoto.set(false);
        this.profileError.set((err?.error?.error as string | undefined) ?? 'Error al subir la foto.');
      },
    });
  }

  onBannerSelected(event: Event): void {
    const file = this.extractFile(event);
    if (!file) return;
    this.uploadingBanner.set(true);
    this.profileApi.uploadBanner(file).subscribe({
      next: (url) => {
        this.bannerPreview.set(url);
        this.uploadingBanner.set(false);
      },
      error: (err) => {
        this.uploadingBanner.set(false);
        this.profileError.set((err?.error?.error as string | undefined) ?? 'Error al subir el banner.');
      },
    });
  }

  addLink(): void {
    if (!this.canAddLink || this.addingLink()) return;
    this.linkError.set('');
    this.addingLink.set(true);

    this.profileApi
      .addEnlace({ plataforma: this.newLinkPlataforma, url: this.newLinkUrl.trim() })
      .subscribe({
        next: (enlace) => {
          this.enlaces.update((prev) => [...prev, enlace]);
          this.newLinkUrl = '';
          this.addingLink.set(false);
        },
        error: (err) => {
          this.addingLink.set(false);
          this.linkError.set((err?.error?.error as string | undefined) ?? 'No se pudo añadir el enlace.');
        },
      });
  }

  deleteLink(id: number): void {
    if (this.deletingLinkId() !== null) return;
    this.deletingLinkId.set(id);

    this.profileApi.deleteEnlace(id).subscribe({
      next: () => {
        this.enlaces.update((prev) => prev.filter((e) => e.id !== id));
        this.deletingLinkId.set(null);
      },
      error: () => {
        this.deletingLinkId.set(null);
      },
    });
  }

  private extractFile(event: Event): File | null {
    const input = event.target as HTMLInputElement;
    return input.files?.[0] ?? null;
  }
}

import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-admin-pagination',
  imports: [],
  templateUrl: './admin-pagination.html',
})
export class AdminPagination {
  @Input() currentPage = 0;
  @Input() totalPages = 0;
  @Input() totalElements = 0;
  @Input() pageSize = 20;
  @Output() pageChange = new EventEmitter<number>();

  get from(): number {
    return this.totalElements === 0 ? 0 : this.currentPage * this.pageSize + 1;
  }

  get to(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }

  prev(): void {
    if (this.currentPage > 0) this.pageChange.emit(this.currentPage - 1);
  }

  next(): void {
    if (this.currentPage < this.totalPages - 1) this.pageChange.emit(this.currentPage + 1);
  }
}

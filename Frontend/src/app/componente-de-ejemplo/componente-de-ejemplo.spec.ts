import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComponenteDeEjemplo } from './componente-de-ejemplo';

describe('ComponenteDeEjemplo', () => {
  let component: ComponenteDeEjemplo;
  let fixture: ComponentFixture<ComponenteDeEjemplo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComponenteDeEjemplo],
    }).compileComponents();

    fixture = TestBed.createComponent(ComponenteDeEjemplo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

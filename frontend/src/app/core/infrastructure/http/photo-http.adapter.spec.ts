import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PhotoHttpAdapter } from './photo-http.adapter';
import { API_BASE_URL } from './api.tokens';

describe('PhotoHttpAdapter', () => {
  let adapter: PhotoHttpAdapter;
  let httpMock: HttpTestingController;
  const base = 'http://api.test';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PhotoHttpAdapter,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: base },
      ],
    });
    adapter = TestBed.inject(PhotoHttpAdapter);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POSTs the file as multipart form data and returns the stored URL', () => {
    const file = new File(['bytes'], 'car.png', { type: 'image/png' });

    let received: string | undefined;
    adapter.upload(file).subscribe((url) => (received = url));

    const req = httpMock.expectOne(`${base}/api/cars/photos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    expect((req.request.body as FormData).get('file')).toBe(file);
    // Letting the browser set the multipart boundary is why we must not set it.
    expect(req.request.headers.has('Content-Type')).toBe(false);

    req.flush({ photoUrl: '/uploads/abc.png' });

    expect(received).toBe('/uploads/abc.png');
  });
});

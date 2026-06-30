import { describe, it, expect } from "vitest";
import {
  createDownload,
  updateDownloadProgress,
  completeDownload,
  pauseDownload,
  resumeDownload,
  cancelDownload,
  getDownloadProgress,
  formatFileSize,
} from "../downloads";

describe("createDownload", () => {
  it("creates a download with pending status", () => {
    const dl = createDownload("https://example.com/file.zip", "file.zip", "/tmp/file.zip", 1000);
    expect(dl.status).toBe("pending");
    expect(dl.receivedBytes).toBe(0);
    expect(dl.totalBytes).toBe(1000);
  });
});

describe("updateDownloadProgress", () => {
  it("updates receivedBytes and sets status to downloading", () => {
    const dl = createDownload("https://example.com/f.zip", "f.zip", "/tmp", 1000);
    const result = updateDownloadProgress([dl], dl.id, 500);
    expect(result[0].receivedBytes).toBe(500);
    expect(result[0].status).toBe("downloading");
  });
});

describe("completeDownload", () => {
  it("sets status to completed and completedAt", () => {
    const dl = createDownload("https://example.com/f.zip", "f.zip", "/tmp", 1000);
    const result = completeDownload([dl], dl.id);
    expect(result[0].status).toBe("completed");
    expect(result[0].completedAt).toBeDefined();
  });
});

describe("pauseDownload / resumeDownload", () => {
  it("pauses a downloading download", () => {
    let dls = [createDownload("https://x.com/f.zip", "f.zip", "/tmp", 500)];
    dls = updateDownloadProgress(dls, dls[0].id, 100);
    dls = pauseDownload(dls, dls[0].id);
    expect(dls[0].status).toBe("paused");
  });

  it("resumes a paused download", () => {
    let dls = [createDownload("https://x.com/f.zip", "f.zip", "/tmp", 500)];
    dls = updateDownloadProgress(dls, dls[0].id, 100);
    dls = pauseDownload(dls, dls[0].id);
    dls = resumeDownload(dls, dls[0].id);
    expect(dls[0].status).toBe("downloading");
  });
});

describe("cancelDownload", () => {
  it("cancels a downloading download", () => {
    let dls = [createDownload("https://x.com/f.zip", "f.zip", "/tmp", 500)];
    dls = updateDownloadProgress(dls, dls[0].id, 100);
    dls = cancelDownload(dls, dls[0].id);
    expect(dls[0].status).toBe("cancelled");
  });
});

describe("getDownloadProgress", () => {
  it("returns 0 for zero total bytes", () => {
    const dl = createDownload("https://x.com/f.zip", "f.zip", "/tmp", 0);
    expect(getDownloadProgress(dl)).toBe(0);
  });

  it("returns correct ratio", () => {
    let dls = [createDownload("https://x.com/f.zip", "f.zip", "/tmp", 1000)];
    dls = updateDownloadProgress(dls, dls[0].id, 250);
    expect(getDownloadProgress(dls[0])).toBeCloseTo(0.25);
  });
});

describe("formatFileSize", () => {
  it("formats bytes correctly", () => {
    expect(formatFileSize(0)).toBe("0 B");
    expect(formatFileSize(1023)).toBe("1023 B");
    expect(formatFileSize(1024)).toBe("1.0 KB");
    expect(formatFileSize(1024 * 1024)).toBe("1.0 MB");
    expect(formatFileSize(1.5 * 1024 * 1024)).toBe("1.5 MB");
  });
});

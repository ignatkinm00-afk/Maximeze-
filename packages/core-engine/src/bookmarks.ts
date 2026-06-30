import type { Bookmark, BookmarkFolder } from "./types";

export function createBookmark(
  url: string,
  title: string,
  options: Partial<Pick<Bookmark, "favicon" | "folderId">> = {}
): Bookmark {
  const now = Date.now();
  return {
    id: generateId(),
    url,
    title,
    favicon: options.favicon,
    folderId: options.folderId,
    createdAt: now,
    updatedAt: now,
  };
}

export function createBookmarkFolder(
  name: string,
  parentId?: string
): BookmarkFolder {
  const now = Date.now();
  return {
    id: generateId(),
    name,
    parentId,
    createdAt: now,
    updatedAt: now,
  };
}

export function exportToHTML(
  bookmarks: Bookmark[],
  folders: BookmarkFolder[]
): string {
  const folderMap = new Map(folders.map((f) => [f.id, f]));
  const lines: string[] = [
    "<!DOCTYPE NETSCAPE-Bookmark-file-1>",
    "<!-- This is an automatically generated file.",
    "     It will be read and overwritten.",
    "     DO NOT EDIT! -->",
    '<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">',
    "<TITLE>Bookmarks</TITLE>",
    "<H1>Bookmarks</H1>",
    "<DL><p>",
  ];

  const topLevel = bookmarks.filter((b) => !b.folderId);
  for (const b of topLevel) {
    const addDate = Math.floor(b.createdAt / 1000);
    lines.push(
      `    <DT><A HREF="${escapeHtml(b.url)}" ADD_DATE="${addDate}">${escapeHtml(b.title)}</A>`
    );
  }

  for (const folder of folders) {
    if (folder.parentId) continue;
    lines.push(`    <DT><H3>${escapeHtml(folder.name)}</H3>`);
    lines.push("    <DL><p>");
    const children = bookmarks.filter((b) => b.folderId === folder.id);
    for (const b of children) {
      const addDate = Math.floor(b.createdAt / 1000);
      lines.push(
        `        <DT><A HREF="${escapeHtml(b.url)}" ADD_DATE="${addDate}">${escapeHtml(b.title)}</A>`
      );
    }
    lines.push("    </DL><p>");
  }

  void folderMap;
  lines.push("</DL><p>");
  return lines.join("\n");
}

export function exportToJSON(
  bookmarks: Bookmark[],
  folders: BookmarkFolder[]
): string {
  return JSON.stringify({ bookmarks, folders }, null, 2);
}

export function importFromJSON(json: string): {
  bookmarks: Bookmark[];
  folders: BookmarkFolder[];
} {
  const parsed = JSON.parse(json) as unknown;
  if (
    typeof parsed !== "object" ||
    parsed === null ||
    !("bookmarks" in parsed) ||
    !("folders" in parsed)
  ) {
    throw new Error("Invalid bookmark JSON format");
  }
  return parsed as { bookmarks: Bookmark[]; folders: BookmarkFolder[] };
}

export function importFromHTML(html: string): {
  bookmarks: Bookmark[];
  folders: BookmarkFolder[];
} {
  const bookmarks: Bookmark[] = [];
  const folders: BookmarkFolder[] = [];
  const now = Date.now();

  // Parse <A HREF="url" ADD_DATE="ts">title</A> patterns
  const linkRegex = /<A\s+HREF="([^"]+)"[^>]*>([^<]*)<\/A>/gi;
  let match: RegExpExecArray | null;

  while ((match = linkRegex.exec(html)) !== null) {
    const [, url, title] = match;
    bookmarks.push({
      id: generateId(),
      url,
      title: decodeHtml(title),
      createdAt: now,
      updatedAt: now,
    });
  }

  // Parse <H3>folder name</H3> patterns
  const folderRegex = /<H3[^>]*>([^<]+)<\/H3>/gi;
  while ((match = folderRegex.exec(html)) !== null) {
    const [, name] = match;
    folders.push({
      id: generateId(),
      name: decodeHtml(name),
      createdAt: now,
      updatedAt: now,
    });
  }

  return { bookmarks, folders };
}

function generateId(): string {
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`;
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function decodeHtml(text: string): string {
  return text
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&#039;/g, "'");
}

#!/usr/bin/env python3
"""Generate the hand-authored pixel textures used by Redry: Nightfall.

This intentionally uses only Python's standard library so contributors do not need Pillow.
"""
from __future__ import annotations

import math
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "src/main/resources/resourcepacks/nightfall"


class Image:
    def __init__(self, width: int, height: int, background=(0, 0, 0, 0)):
        self.width = width
        self.height = height
        self.pixels = [background] * (width * height)

    def pixel(self, x: int, y: int, color):
        if 0 <= x < self.width and 0 <= y < self.height:
            self.pixels[y * self.width + x] = color

    def rect(self, x: int, y: int, width: int, height: int, color):
        for py in range(y, y + height):
            for px in range(x, x + width):
                self.pixel(px, py, color)

    def line(self, points, color):
        for x, y in points:
            self.pixel(x, y, color)

    def save(self, path: Path):
        path.parent.mkdir(parents=True, exist_ok=True)
        raw = bytearray()
        for y in range(self.height):
            raw.append(0)  # PNG filter: None
            for x in range(self.width):
                raw.extend(self.pixels[y * self.width + x])

        def chunk(kind: bytes, data: bytes) -> bytes:
            return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

        png = b"\x89PNG\r\n\x1a\n"
        png += chunk(b"IHDR", struct.pack(">IIBBBBB", self.width, self.height, 8, 6, 0, 0, 0))
        png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
        png += chunk(b"IEND", b"")
        path.write_bytes(png)


def paint_face(image: Image, x: int, y: int, color, shade, dark):
    # Classic 64x64 humanoid skin UV: the front face is 8x8 at (8, 8).
    image.rect(x, y, 8, 8, color)
    image.rect(x, y, 8, 1, shade)
    image.rect(x, y + 7, 8, 1, shade)
    image.rect(x, y, 1, 8, shade)
    image.rect(x + 7, y, 1, 8, shade)
    # recessed mouth / nose pixels
    image.pixel(x + 3, y + 5, dark)
    image.pixel(x + 4, y + 5, dark)
    image.pixel(x + 5, y + 6, shade)


def box_uv(image: Image, u: int, v: int, width: int, height: int, depth: int, front, side, back, top, bottom):
    # Paint the five visible UV faces for a standard Minecraft cuboid.
    # front: u+depth .. u+depth+width, v+depth .. v+depth+height
    image.rect(u + depth, v + depth, width, height, front)
    image.rect(u, v + depth, depth, height, side)
    image.rect(u + depth + width, v + depth, depth, height, side)
    image.rect(u + depth + width + depth, v + depth, width, height, back)
    image.rect(u + depth, v, width, depth, top)
    image.rect(u + depth + width, v, width, depth, bottom)


def herobrine_skin():
    image = Image(64, 64)
    skin = (166, 174, 178, 255)
    skin_light = (205, 211, 210, 255)
    skin_shadow = (92, 104, 111, 255)
    hair = (13, 17, 23, 255)
    hair_hi = (35, 42, 52, 255)
    void = (5, 7, 11, 255)
    red = (116, 13, 28, 255)
    red_hi = (215, 35, 46, 255)
    steel = (76, 87, 99, 255)
    coat = (23, 28, 37, 255)
    coat_hi = (42, 48, 57, 255)
    pants = (20, 25, 32, 255)

    # Head cube, UV at (0, 0). Side/back planes are deliberately almost black.
    image.rect(0, 0, 32, 16, hair)
    image.rect(8, 0, 8, 8, hair_hi)             # crown
    image.rect(16, 0, 8, 8, void)               # underside
    image.rect(0, 8, 8, 8, hair)
    paint_face(image, 8, 8, skin, skin_shadow, (60, 24, 32, 255))
    image.rect(16, 8, 8, 8, skin_shadow)
    image.rect(24, 8, 8, 8, hair)
    # Uneven fringe; the face has two cold, almost-white eyes.
    image.rect(8, 8, 8, 2, hair)
    image.pixel(9, 10, hair_hi); image.pixel(10, 10, hair)
    image.pixel(14, 10, hair_hi)
    image.rect(9, 11, 2, 1, (245, 247, 239, 255))
    image.rect(13, 11, 2, 1, (245, 247, 239, 255))
    image.pixel(10, 12, (93, 220, 239, 255))
    image.pixel(14, 12, (93, 220, 239, 255))
    image.rect(11, 14, 2, 1, (67, 74, 82, 255))
    # Small scar across one temple.
    image.line([(22, 10), (21, 11), (20, 12)], red)

    # Torso coat; a narrow red signal seam is the visual signature.
    box_uv(image, 16, 16, 8, 12, 4, coat, void, (14, 17, 23, 255), coat_hi, void)
    image.rect(20, 20, 8, 12, coat)
    image.rect(22, 20, 1, 12, red)
    image.rect(23, 22, 1, 2, red_hi)
    image.rect(19, 24, 1, 5, steel)
    image.rect(24, 21, 2, 1, steel)
    # A broken stitched mark: REDRY's archive symbol.
    image.line([(20, 27), (21, 27), (21, 28), (22, 28), (22, 29), (23, 29)], red_hi)
    image.rect(20, 18, 1, 2, steel)
    image.rect(24, 18, 1, 2, steel)

    # Right arm (classic wide-arm skin layout at U=40,V=16).
    box_uv(image, 40, 16, 4, 12, 4, coat, void, (17, 20, 27, 255), coat_hi, void)
    image.rect(44, 20, 4, 8, coat)
    image.rect(44, 20, 1, 8, red)
    image.rect(44, 28, 4, 4, skin_shadow)
    image.rect(48, 28, 1, 4, skin)
    image.rect(52, 28, 4, 4, skin_shadow)
    # Left arm, same silhouette but with a ragged cuff and red thread.
    box_uv(image, 32, 48, 4, 12, 4, coat, void, (16, 20, 27, 255), coat_hi, void)
    image.rect(36, 52, 4, 8, coat)
    image.rect(36, 58, 4, 2, red)
    image.rect(36, 60, 4, 4, skin_shadow)
    image.rect(40, 60, 1, 4, skin)

    # Trousers and worn boots.
    box_uv(image, 0, 16, 4, 12, 4, pants, void, (14, 18, 24, 255), coat_hi, void)
    image.rect(4, 20, 4, 12, pants)
    image.rect(5, 23, 1, 4, steel)
    image.rect(0, 28, 4, 4, (11, 14, 19, 255))
    image.rect(4, 28, 4, 4, (10, 12, 17, 255))
    box_uv(image, 16, 48, 4, 12, 4, pants, void, (14, 18, 24, 255), coat_hi, void)
    image.rect(20, 52, 4, 12, pants)
    image.rect(21, 54, 1, 5, red)
    image.rect(16, 60, 4, 4, (10, 12, 17, 255))
    image.rect(20, 60, 4, 4, (9, 11, 15, 255))

    # Extra UV islands for the ragged shoulder mantle, jaw plate, and signal spine.
    image.rect(0, 32, 43, 11, coat)
    image.rect(0, 32, 43, 1, steel)
    image.rect(8, 35, 32, 1, void)
    image.rect(20, 33, 1, 9, red)
    image.rect(21, 35, 1, 3, red_hi)
    image.rect(46, 32, 6, 2, void)
    image.rect(48, 32, 2, 1, red_hi)
    image.rect(56, 48, 7, 11, coat)
    image.rect(58, 49, 1, 9, red)
    image.pixel(60, 51, red_hi); image.pixel(60, 55, steel)
    return image


def still_one_skin():
    image = Image(64, 64)
    stone = (181, 178, 163, 255)
    stone_hi = (216, 211, 191, 255)
    stone_shadow = (91, 91, 86, 255)
    black = (9, 10, 12, 255)
    rust = (104, 30, 29, 255)
    rust_hi = (174, 43, 34, 255)
    gray = (54, 57, 58, 255)

    # Chalk-white head with an empty, off-center face cavity.
    image.rect(0, 0, 32, 16, stone_shadow)
    image.rect(8, 0, 8, 8, stone_hi)
    image.rect(16, 0, 8, 8, gray)
    image.rect(0, 8, 8, 8, stone_shadow)
    paint_face(image, 8, 8, stone, stone_shadow, black)
    image.rect(16, 8, 8, 8, stone_shadow)
    image.rect(24, 8, 8, 8, gray)
    image.rect(8, 8, 8, 2, stone_hi)
    image.rect(10, 11, 4, 4, black)
    image.pixel(11, 11, rust)
    image.pixel(12, 14, rust_hi)
    image.line([(20, 9), (19, 10), (19, 11), (18, 12), (18, 13), (17, 14)], rust)

    # Cracked stone torso and limbs, with a red sealed-number stamp.
    box_uv(image, 16, 16, 8, 12, 4, stone, stone_shadow, gray, stone_hi, stone_shadow)
    image.rect(20, 20, 8, 12, stone)
    image.line([(21, 20), (22, 21), (22, 23), (23, 24), (22, 26), (24, 27), (24, 29), (25, 30)], rust)
    image.rect(24, 22, 2, 1, rust_hi)
    image.rect(19, 25, 1, 3, gray)
    image.rect(25, 20, 1, 2, stone_hi)

    for u, v, face_x, face_y in [(40, 16, 44, 20), (32, 48, 36, 52)]:
        box_uv(image, u, v, 4, 12, 4, stone, stone_shadow, gray, stone_hi, stone_shadow)
        image.rect(face_x, face_y, 4, 8, stone)
        image.line([(face_x + 1, face_y + 1), (face_x + 1, face_y + 3),
                    (face_x + 2, face_y + 4), (face_x + 1, face_y + 6)], rust)
        image.rect(face_x, face_y + 8, 4, 4, stone_shadow)

    for u, v in [(0, 16), (16, 48)]:
        box_uv(image, u, v, 4, 12, 4, gray, stone_shadow, black, stone_hi, black)
        image.line([(u + 6, v + 6), (u + 5, v + 8), (u + 6, v + 10), (u + 5, v + 12)], rust)

    image.rect(0, 32, 43, 11, stone)
    image.rect(0, 32, 43, 1, stone_hi)
    image.line([(3, 32), (4, 34), (3, 36), (6, 39), (5, 42)], rust)
    image.line([(15, 32), (14, 35), (16, 37), (14, 40)], stone_shadow)
    image.rect(46, 32, 6, 2, stone_shadow)
    image.rect(48, 32, 2, 1, rust_hi)
    image.rect(56, 48, 7, 11, stone_shadow)
    image.line([(58, 48), (59, 51), (58, 54), (60, 56), (59, 58)], rust)
    return image


def chat_echo_skin():
    image = Image(64, 64)
    void = (7, 6, 11, 255)
    ink = (25, 15, 31, 255)
    pale = (185, 185, 196, 255)
    pale_hi = (232, 229, 221, 255)
    violet = (101, 30, 123, 255)
    violet_hi = (208, 69, 208, 255)
    red = (170, 24, 48, 255)
    cyan = (65, 198, 211, 255)
    gray = (59, 52, 67, 255)

    # A paper-white mask on a body that seems to have been cut out of a black screen.
    image.rect(0, 0, 32, 16, void)
    image.rect(8, 0, 8, 8, ink)
    image.rect(16, 0, 8, 8, void)
    image.rect(0, 8, 8, 8, ink)
    image.rect(8, 8, 8, 8, pale)
    image.rect(16, 8, 8, 8, pale_hi)
    image.rect(24, 8, 8, 8, void)
    image.rect(8, 8, 8, 2, gray)
    # Eyes are offset by one pixel; the missing pixel is the uncomfortable part.
    image.rect(9, 11, 2, 1, void)
    image.rect(13, 12, 2, 1, void)
    image.pixel(10, 11, red)
    image.pixel(14, 12, violet_hi)
    image.line([(11, 13), (10, 14), (12, 15)], gray)
    image.line([(21, 9), (20, 10), (20, 11), (19, 12), (19, 13), (18, 14)], violet)

    # The torso carries deliberately unreadable chat fragments and a torn signal seam.
    box_uv(image, 16, 16, 8, 12, 4, ink, void, violet, gray, void)
    image.rect(20, 20, 8, 12, ink)
    image.rect(20, 21, 6, 1, pale_hi)
    image.rect(22, 23, 4, 1, violet_hi)
    image.rect(21, 25, 7, 1, cyan)
    image.rect(20, 27, 5, 1, pale)
    image.rect(24, 29, 4, 1, red)
    image.line([(20, 20), (21, 22), (20, 24), (22, 26), (21, 28), (23, 31)], violet_hi)
    image.rect(27, 22, 1, 2, red)

    # Long, mismatched sleeves end in hands that are almost too pale to read.
    for u, v, fx, fy in [(40, 16, 44, 20), (32, 48, 36, 52)]:
        box_uv(image, u, v, 4, 12, 4, ink, void, violet, gray, void)
        image.rect(fx, fy, 4, 8, ink)
        image.rect(fx, fy + 2, 4, 1, cyan if u == 40 else violet_hi)
        image.rect(fx, fy + 8, 4, 4, pale)
        image.rect(fx + 1, fy + 9, 1, 3, pale_hi)

    # The legs do not quite line up with the body's centre.
    for u, v in [(0, 16), (16, 48)]:
        box_uv(image, u, v, 4, 12, 4, void, ink, void, gray, void)
        image.rect(u + 5, v + 5, 1, 2, violet_hi)
        image.rect(u + 6, v + 9, 1, 1, cyan)

    image.rect(0, 32, 43, 11, ink)
    image.rect(0, 32, 43, 1, violet)
    image.rect(4, 35, 37, 1, pale_hi)
    image.rect(12, 38, 21, 1, cyan)
    image.rect(6, 41, 35, 1, violet_hi)
    image.rect(46, 32, 6, 2, void)
    image.rect(47, 32, 3, 1, red)
    image.rect(56, 48, 7, 11, void)
    image.rect(58, 49, 1, 8, cyan)
    image.rect(60, 51, 1, 1, violet_hi)
    image.rect(59, 55, 2, 1, red)
    return image


def receiver_icon():
    im = Image(16, 16, (0, 0, 0, 0))
    im.rect(3, 1, 10, 2, (163, 137, 87, 255))
    im.rect(2, 3, 12, 11, (19, 22, 27, 255))
    im.rect(3, 4, 10, 8, (45, 42, 39, 255))
    im.rect(4, 5, 8, 6, (13, 16, 21, 255))
    im.rect(5, 6, 6, 1, (97, 29, 37, 255))
    im.rect(6, 7, 4, 1, (207, 39, 55, 255))
    im.rect(8, 8, 1, 2, (245, 76, 75, 255))
    im.rect(4, 12, 8, 1, (137, 115, 78, 255))
    im.rect(5, 13, 6, 1, (91, 69, 50, 255))
    im.rect(6, 14, 4, 1, (39, 33, 29, 255))
    im.pixel(4, 3, (207, 191, 145, 255))
    im.pixel(11, 3, (207, 191, 145, 255))
    return im


def cassette_icon():
    im = Image(16, 16, (0, 0, 0, 0))
    im.rect(1, 2, 14, 12, (12, 14, 18, 255))
    im.rect(2, 1, 12, 1, (105, 111, 115, 255))
    im.rect(2, 3, 12, 2, (45, 49, 53, 255))
    im.rect(3, 6, 10, 5, (21, 24, 28, 255))
    im.rect(4, 7, 3, 3, (117, 24, 38, 255))
    im.rect(9, 7, 3, 3, (117, 24, 38, 255))
    im.rect(5, 8, 1, 1, (222, 207, 169, 255))
    im.rect(10, 8, 1, 1, (222, 207, 169, 255))
    im.rect(4, 12, 8, 1, (80, 86, 90, 255))
    im.rect(6, 12, 4, 1, (168, 35, 44, 255))
    im.rect(2, 14, 12, 1, (6, 7, 9, 255))
    return im


def pack_icon():
    im = Image(128, 128, (5, 7, 10, 255))
    # Pixelated concentric warning rings and a single cold eye.
    for inset, color in [(9, (24, 29, 37, 255)), (17, (48, 18, 29, 255)), (25, (107, 18, 34, 255))]:
        im.rect(inset, inset, 128 - inset * 2, 2, color)
        im.rect(inset, 126 - inset, 128 - inset * 2, 2, color)
        im.rect(inset, inset, 2, 128 - inset * 2, color)
        im.rect(126 - inset, inset, 2, 128 - inset * 2, color)
    im.rect(25, 48, 78, 31, (14, 17, 22, 255))
    im.rect(33, 55, 22, 12, (224, 230, 224, 255))
    im.rect(73, 55, 22, 12, (224, 230, 224, 255))
    im.rect(42, 58, 8, 7, (50, 207, 222, 255))
    im.rect(82, 58, 8, 7, (50, 207, 222, 255))
    im.rect(56, 82, 16, 3, (139, 17, 36, 255))
    im.rect(61, 86, 7, 5, (139, 17, 36, 255))
    # Signal bars at the bottom.
    for x, h in [(34, 9), (45, 16), (56, 24), (67, 33), (78, 24), (89, 16)]:
        im.rect(x, 111 - h, 5, h, (109, 22, 39, 255))
    im.rect(28, 112, 72, 2, (174, 31, 45, 255))
    return im


if __name__ == "__main__":
    herobrine_skin().save(PACK / "assets/redry/textures/entity/herobrine.png")
    still_one_skin().save(PACK / "assets/redry/textures/entity/still_one.png")
    chat_echo_skin().save(PACK / "assets/redry/textures/entity/chat_echo.png")
    cassette_icon().save(PACK / "assets/redry/textures/item/field_tape.png")
    receiver_icon().save(PACK / "assets/redry/textures/item/signal_receiver.png")
    pack_icon().save(PACK / "pack.png")
    # Keep both pack and Fabric mod-list badges synchronized.
    icon = (PACK / "pack.png").read_bytes()
    (PACK / "assets/redry/icon.png").write_bytes(icon)
    (ROOT / "src/main/resources/assets/redry/icon.png").write_bytes(icon)
    print("Generated three anomaly skins, two archival item icons, and Nightfall badges.")

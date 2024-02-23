
void main() {
    ivec2 uv;
    int r = (uv.x >> 8) & 0xFF;
    int g = (uv.x) & 0xFF;
    int b = (uv.y >> 8) & 0xFF;
    int sky = (uv.y) & 0xFF;
}

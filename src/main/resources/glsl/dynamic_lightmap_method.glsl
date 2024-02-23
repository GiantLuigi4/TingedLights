
#define SLOTS 100
uniform sampler2D TingedLights_LightTexture;
uniform vec4[SLOTS] TingedLights_LightCoords;
uniform  int[SLOTS] TingedLights_LightColors;

int dynamic_lights_coord(vec3 vert, vec3 origin) {
    vec3 pos = vert - origin;

    float d = 2387.0;
    for (float x = 0.0; x <= 1.0; x++) {
        for (float y = 0.0; y <= 1.0; y++) {
            for (float z = 0.0; z <= 1.0; z++) {
                float d1 = abs(round(pos.x + x - 0.5)) + abs(round(pos.y + y - 0.5)) + abs(round(pos.z + z - 0.5));
                d = min(d, d1);
            }
        }
    }

    #define SIZE 15.0
    d = clamp(d, 0.0, SIZE) / SIZE;
    int index = int(floor(d * 15.0));

    return index;
}

vec4 dynamic_lights_sample_light(vec4 pos, sampler2D lightMap, ivec2 uv) {
    int index = dynamic_lights_coord(pos.xyz, vec3(0, 0, 0));
    vec4 mc = minecraft_sample_lightmap(lightMap, uv);

    //vec3 v3 = (TingedLights_LightColors[index] * 5) + 1;
    //vec4 outV = mc / vec4(v3, 0);

    vec4 outV = mc;
    if (index != 15)
        outV += vec4(texture2D(TingedLights_LightTexture,
           vec2(float(index) / 15.0, 0.0)
        ).xyz, 0.0);

    for (int i = 0; i < SLOTS; i++) {
        int color = TingedLights_LightColors[i];
        if (color != 0) {
            vec4 c = TingedLights_LightCoords[i];
            int i1 = dynamic_lights_coord(pos.xyz, c.xyz);

            i1 += int(c.w);
            if (i1 < 15)
                outV += vec4(texture2D(TingedLights_LightTexture,
                    vec2(float(i1) / 15.0, float(color) / 100.0)
                ).xyz, 0.0);
        }
    }

    outV = clamp(outV, 0.0, 1.0);
    return outV;
}
vec4 extract_matrix_scale(mat4 matr) {
    return vec4(
        length(vec3(matr[0][0], matr[0][1], matr[0][2])),
        length(vec3(matr[1][0], matr[1][1], matr[1][2])),
        length(vec3(matr[2][0], matr[2][1], matr[2][2])),
        1
    );
}
vec3 extract_matrix_offset(mat4 matr){ return matr[3].xyz; }

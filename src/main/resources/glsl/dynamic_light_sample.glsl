#ifdef Position
dynamic_lights_sample_light((
#ifdef ModelViewMat
extract_matrix_scale(ModelViewMat)*
#endif
vec4((Position).xyz
#ifdef ChunkOffset
+ChunkOffset
#endif
-TingedLights_CameraOffset
#ifdef ModelViewMat
+extract_matrix_offset(ModelViewMat)
#endif
, 1)
),
#else
minecraft_sample_lightmap(
#endif

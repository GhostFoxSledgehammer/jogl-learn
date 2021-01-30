#version 430
uniform float angle;
mat4 buildRotateX(float rad)
{ mat4 xrot = mat4( 1.0, 0.0, 0.0, 0.0,
0.0, cos(rad), -sin(rad), 0.0,
0.0, sin(rad), cos(rad), 0.0,
0.0, 0.0, 0.0, 1.0 );
return xrot;
}
void main(void)
{
mat4 rotationxmatrix = buildRotateX(angle); 
if (gl_VertexID == 0) gl_Position = rotationxmatrix * vec4(0.25, -0.25, 0.0, 1.0);
else if (gl_VertexID == 1) gl_Position = rotationxmatrix * vec4(-0.25, -0.25, 0.0, 1.0);
else gl_Position = rotationxmatrix * vec4(0.25, 0.25, 0.0, 1.0);
}
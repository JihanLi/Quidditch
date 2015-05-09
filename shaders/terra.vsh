varying vec3 lightDir, normal, view;
varying float height, offset0, offset1;

attribute float snowOffset;
attribute float grassOffset;

void main()
{
	lightDir = normalize(gl_LightSource[0].position.xyz);
	normal = normalize(gl_NormalMatrix * gl_Normal);
	
	vec4 viewCamera = gl_ModelViewMatrix * gl_Vertex;
	viewCamera /= viewCamera.w;
	vec3 view = normalize(-vec3(view));
	
	height = gl_Vertex.y;
	
	offset0 = snowOffset;
	offset1 = grassOffset;
	
	gl_Position = ftransform();
	gl_TexCoord[0] = gl_MultiTexCoord0;  
}
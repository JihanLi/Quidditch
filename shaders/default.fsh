varying vec3 lightDir, normal, view;

uniform sampler2D tex;
uniform int hasTex;

void main()
{
	vec4 ambient = (gl_LightModel.ambient + gl_LightSource[0].ambient) * gl_FrontMaterial.ambient;
	vec4 diffuse = gl_LightSource[0].diffuse * gl_FrontMaterial.diffuse;
	vec4 specular = gl_LightSource[0].specular * gl_FrontMaterial.specular;
	
	float ndotl = max(dot(normal, lightDir), 0.0);
	vec4 cl = ambient;
	cl += ndotl * diffuse;
	
	vec4 color;
	
	if (hasTex == 1)
	{
		vec4 ct = texture2D(tex, gl_TexCoord[0].st);
		color = cl * ct;
	}
	else
	{
		color = cl;
	}
	
	vec3 halfVector = normalize(lightDir + view);
	
	float ndothv = max(dot(normal, halfVector), 0.0);
	
	float alpha = color.a;
	color += specular * pow(ndothv, gl_FrontMaterial.shininess);
	color.a = alpha;
	
	gl_FragColor = color;
}
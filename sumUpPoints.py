import sys
import json

ai_names = input("Please input AI names in format: '[<ai1>, <ai2>, ...]'\n")
avg_points = input("Please input average points in format: '[<ai1_avg_points>, <ai2_avg_points>, ...]'\n")

# Remove brackets
ai_names = ai_names[1:-1]
avg_points = avg_points[1:-1]

ai_names = ai_names.split(',')
avg_points = avg_points.split(',')

try:
	open('summed_values.txt', 'x')	# Tries to create the file
except:
	pass
f = open('summed_values.txt', 'r+')
contents = f.read()
f.close()
if(contents == ""):
	scores = {}
else:
	contents = contents.replace("'", '"')	# Python requires double quotes
	scores = json.loads(contents)

for i in range(0, len(ai_names)):
	ai_name = ai_names[i]
	if ai_name in scores:
		scores[ai_name] += float(avg_points[i])
	else:
		scores[ai_name] = float(avg_points[i])

print("Scores so far: " + str(scores))
f = open('summed_values.txt', 'w')
f.write(str(scores))
f.close()
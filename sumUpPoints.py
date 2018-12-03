import sys
import json
import operator

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

done_inputting = False
while(not done_inputting):
	ai_names = input("\nPlease input AI names in format: '[<ai1>, <ai2>, ...]' (enter x to stop)\n")
	avg_points = input("Please input average points in format: '[<ai1_avg_points>, <ai2_avg_points>, ...] (enter x to stop)'\n")

	if(ai_names == 'x' or avg_points == 'x'):
		done_inputting = True
		break

	# Remove brackets and split by commas
	ai_names = ai_names[1:-1].split(',')
	avg_points = avg_points[1:-1].split(',')

	# Sum up nodes
	for i in range(0, len(ai_names)):
		ai_name = ai_names[i]
		if ai_name in scores:
			scores[ai_name] += float(avg_points[i])
		else:
			scores[ai_name] = float(avg_points[i])

# Note: converts to list so don't write this to the file. Only for display purposes
sorted_scores = sorted(scores.items(), key=operator.itemgetter(1), reverse=True)
print("Average Points for far:")
for score in sorted_scores:
	print("\t" + score[0] + ": " + str(score[1]))

f = open('summed_values.txt', 'w')
f.write(str(scores))
f.close()
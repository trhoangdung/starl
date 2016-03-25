function color = getColor(imgColor, center)
colors = ['r', 'g', 'b'];
centerRounded = round(center);
x = centerRounded(1,2);
y = centerRounded(1,1);
if(imgColor(x,y,1) > 100 && imgColor(x,y,2) > 100 && imgColor(x,y,3) > 100)
    color = 'w';
else
    [~,colorIndex] = max(imgColor(centerRounded(1,2), centerRounded(1,1), :));
    color = colors(colorIndex(1));
end
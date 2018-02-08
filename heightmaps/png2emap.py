#!/usr/bin/python

import os
import sys
import png

rdr = png.Reader(filename=sys.argv[1])
w,h,img,meta = rdr.read()

# making some assumptions about the file
assert w==h
assert not meta['greyscale']
assert meta['planes'] == 3 or meta['planes'] == 4

# we only care about the red channel values
skip = 4 if meta['alpha'] else 3

f = open('%s.emap' % sys.argv[1], 'wt')
f.write('%d\n' % w)
img = list(img)
for row in img:
    for i in range(0,w):
        v = row[i*skip]
        f.write('%d ' % v)
    f.write('\n')
f.close()

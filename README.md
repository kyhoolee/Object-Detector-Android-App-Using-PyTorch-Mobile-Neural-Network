# Object Detector Android App Using PyTorch Mobile Neural Network

Check out the tutorial at: https://towardsdatascience.com/object-detector-android-app-using-pytorch-mobile-neural-network-407c419b56cd



## Files/Folder
 1. ObjectDetectorDemo - Android Application.
 2. create_model.py - To create the PyTorch model.


## Libraries needed for PyTorch model

- pip install torchvision


ssh hongky@galadriel.it.deakin.edu.au -L 8888:localhost:8888
jupyter notebook > log.txt 2>&1 &








resnet101

- Simulator
172.888.064 Mb
1.118 s

173.232.128 Mb
1.095 s

186.314.752 Mb
1.082 s


- Real-device
188.600.320 Mb
3.206 s

188.764.160 Mb
3.194 s

189.136.896 Mb
3.190 s



vgg19
- real-device
161.189.888
3.301

198.643.712
3.112

203.882.496
3.682


- simulator
555.933.696 Mb
1.742 s

533.233.664 Mb
3.170 s

539.951.104 Mb
3.072 s

561.352.704 Mb
2.643 s


https://colab.research.google.com/github/pytorch/tutorials/blob/gh-pages/_downloads/quantized_transfer_learning_tutorial.ipynb#scrollTo=WqLJjE5rrVNd
https://github.com/pytorch/vision/issues/1943





https://github.com/jakc4103/DFQ
https://github.com/submission2019/cnn-quantization
https://github.com/Mxbonn/INQ-pytorch
https://github.com/microsoft/LQ-Nets

--> these papers and repos do not report clearly about the speed improvement
Only focus on accuracy and compress-ratio (model size)



https://pytorch.org/tutorials/intermediate/quantized_transfer_learning_tutorial.html

--> Official Pytorch supports Quantization - but only an experimental version
--> the quantized model can not run on Mobile
https://github.com/pytorch/vision/issues/1943




Tensorflow lite 
https://www.tensorflow.org/lite/guide/hosted_models

--> Show clearly model-size and speed and also give pretrained and quantized model 



My opinion: 

- If we want to get the mobile-ready model, I will focus on tensorflow lite first, learn how to build model on TF, learn to convert model to Tf-lite, and convert quantized model 

- If we want to explore compression-idea, only focus on compress-ratio vs accuracy tradeoff, and do not care much about mobile-ready model and speed, I will focus on pytorch repo, try to reproduct paper reports.
import torch
import torchvision
resnet18 = torchvision.models.mobilenet_v2(pretrained=True)
resnet18.eval()
example_inputs = torch.rand(1, 3, 224, 224)
resnet18_traced = torch.jit.trace(resnet18, example_inputs = example_inputs)
resnet18_traced.save("mobilenet_v2_traced.pt")
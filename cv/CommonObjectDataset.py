import os
import torch
from PIL import Image

class CommonObjectDataset(torch.utils.data.Dataset):
    def __init__(self, root, transforms=None):
        self.root = root
        self.transforms = transforms
        self.imgs = sorted(os.listdir(os.path.join(root, "images")))
        self.annots = sorted(os.listdir(os.path.join(root, "annotations")))

    def __getitem__(self, idx):
        # Load image
        img_path = os.path.join(self.root, "images", self.imgs[idx])
        img = Image.open(img_path).convert("RGB")

        # Load and parse annotation (this could be XML, JSON, etc.)
        # For example, parse an XML and extract boxes and labels:
        # (Assume your parse_annotation function returns a dict)
        target = parse_annotation(os.path.join(self.root, "annotations", self.annots[idx]))

        if self.transforms is not None:
            img = self.transforms(img)
        return img, target

    def __len__(self):
        return len(self.imgs)

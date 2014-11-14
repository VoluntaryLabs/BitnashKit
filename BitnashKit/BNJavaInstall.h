//
//  BNJavaInstall.h
//  BitnashKit
//
//  Created by Steve Dekorte on 11/9/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BNJavaInstall : NSObject

- (BOOL)isInstalled;
- (void)openJavaDmg;

@end

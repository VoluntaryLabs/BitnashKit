//
//  BNMetaDataDb.h
//  BitnashKit
//
//  Created by Rich Collins on 5/23/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNObject.h"

@interface BNMetaDataDb : NSObject

@property NSString *path;

+ (BNMetaDataDb *)shared;

- (void)readToBnObject:(BNObject *)bnObject;
- (void)writeFromBnObject:(BNObject *)bnObject;

@end
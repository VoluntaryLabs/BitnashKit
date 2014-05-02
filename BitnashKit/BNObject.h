//
//  BNObject.h
//  BitnashKit
//
//  Created by Rich Collins on 4/16/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <NavKit/NavKit.h>

@class BNError;

@interface BNObject : NavInfoNode

@property NSMutableArray *serializedSlotNames;
@property BNError *error;

- (id)asJSONObject;

- (id)asObjectFromJSONObject;

- (void)awakeFromJSONDict:(NSDictionary *)dict;

- (void)writeToFile:(NSString *)filePath;

- (void)copySlotsFrom:(BNObject *)other;

@end
